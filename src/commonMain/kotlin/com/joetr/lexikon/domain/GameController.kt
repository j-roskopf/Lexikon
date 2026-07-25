package com.joetr.lexikon.domain

import com.joetr.lexikon.LexikonServices
import com.joetr.lexikon.data.PersistenceRepository
import com.joetr.lexikon.model.Difficulty
import com.joetr.lexikon.model.GameMode
import com.joetr.lexikon.model.GameSnapshot
import com.joetr.lexikon.model.GameStatus
import com.joetr.lexikon.model.GuessRow
import com.joetr.lexikon.model.LengthStats
import com.joetr.lexikon.model.LetterMark
import com.joetr.lexikon.model.PlayerSettings
import com.joetr.lexikon.model.StatsKey
import com.joetr.lexikon.model.Tile
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

sealed class GameMessage {
    data object None : GameMessage()
    data class InvalidWord(val word: String) : GameMessage()
    data object HardModeViolation : GameMessage()
    data object NotEnoughLetters : GameMessage()
    data object Copied : GameMessage()
}

class GameController(
    private val services: LexikonServices,
    private val dictionary: DictionaryRepository,
    private val persistence: PersistenceRepository,
    initialSettings: PlayerSettings,
    initialRoute: WebRouteParser.Route,
) {
    var settings by mutableStateOf(initialSettings)
        private set

    var submittedMarks by mutableStateOf<List<List<LetterMark>>>(emptyList())
        private set

    var submittedGuesses by mutableStateOf<List<String>>(emptyList())
        private set

    var snapshot by mutableStateOf(
        createInitialSnapshot(
            initialRoute.mode,
            initialRoute.length,
            initialRoute.difficulty,
            initialSettings,
        )
    )
        private set

    var stats by mutableStateOf(persistence.loadStats())
        private set

    var message by mutableStateOf<GameMessage>(GameMessage.None)
        private set

    var showHelp by mutableStateOf(!initialSettings.hasSeenHelp)
        private set

    var showStats by mutableStateOf(false)
        private set

    var showSettings by mutableStateOf(false)
        private set

    var showNextPuzzleCountdown by mutableStateOf(false)
        private set

    var pendingConfirm by mutableStateOf<PendingChange?>(null)
        private set

    val keyboardMarks: Map<Char, LetterMark>
        get() = GuessEngine.keyboardMarks(submittedMarks, submittedGuesses)

    private fun createInitialSnapshot(
        mode: GameMode,
        length: Int,
        difficulty: Difficulty,
        settings: PlayerSettings,
    ): GameSnapshot {
        val today = services.clock.today()
        val dateStr = today.toString()
        if (mode == GameMode.Daily) {
            persistence.loadDaily(length, difficulty, dateStr)?.let { stored ->
                restoreSubmitted(stored)
                return stored
            }
        }
        val answer = when (mode) {
            // Sorted so the daily word depends only on the seed, not on file order.
            GameMode.Daily -> DailyPuzzleSelector.dailyAnswer(
                today,
                length,
                difficulty,
                dictionary.answers(length, difficulty).toList().sorted(),
            )
            GameMode.Free -> dictionary.randomAnswer(
                length,
                difficulty,
                services.seededRandom ?: kotlin.random.Random.Default,
            )
        }
        val snap = createGameSnapshot(
            mode,
            length,
            difficulty,
            answer,
            settings.hardMode,
            if (mode == GameMode.Daily) dateStr else null,
        )
        submittedMarks = emptyList()
        submittedGuesses = emptyList()
        if (mode == GameMode.Daily) persistence.saveDaily(snap)
        return snap
    }

    private fun restoreSubmitted(stored: GameSnapshot) {
        val guesses = mutableListOf<String>()
        val marks = mutableListOf<List<LetterMark>>()
        for (row in stored.rows) {
            val letters = row.tiles.mapNotNull { it.char }.joinToString("")
            if (letters.length == stored.wordLength) {
                guesses += letters
                marks += row.tiles.map { it.mark }.filter { it != LetterMark.Empty && it != LetterMark.Tbd }
            }
        }
        submittedGuesses = guesses
        submittedMarks = marks
    }

    fun type(char: Char) {
        if (snapshot.status != GameStatus.Playing) return
        val upper = char.uppercaseChar()
        if (upper !in 'A'..'Z') return
        if (snapshot.currentInput.length >= snapshot.wordLength) return
        snapshot = snapshot.copy(currentInput = snapshot.currentInput + upper)
        message = GameMessage.None
    }

    fun backspace() {
        if (snapshot.status != GameStatus.Playing) return
        if (snapshot.currentInput.isEmpty()) return
        snapshot = snapshot.copy(currentInput = snapshot.currentInput.dropLast(1))
    }

    fun submit() {
        if (snapshot.status != GameStatus.Playing) return
        val guess = snapshot.currentInput.uppercase()
        if (guess.length < snapshot.wordLength) {
            message = GameMessage.NotEnoughLetters
            return
        }
        if (!dictionary.isValidGuess(snapshot.wordLength, guess)) {
            message = GameMessage.InvalidWord(guess)
            return
        }
        if (snapshot.hardMode && !GuessEngine.isHardModeValid(
                snapshot.answer,
                submittedGuesses,
                submittedMarks,
                guess,
            )
        ) {
            message = GameMessage.HardModeViolation
            return
        }
        val marks = GuessEngine.evaluate(snapshot.answer, guess)
        submittedGuesses = submittedGuesses + guess
        submittedMarks = submittedMarks + listOf(marks)

        val rowIndex = submittedGuesses.size - 1
        val newRows = snapshot.rows.toMutableList()
        newRows[rowIndex] = GuessRow(marks.mapIndexed { i, mark ->
            Tile(guess[i], mark)
        })

        val won = marks.all { it == LetterMark.Correct }
        val lost = !won && submittedGuesses.size >= snapshot.maxGuesses
        val newStatus = when {
            won -> GameStatus.Won
            lost -> GameStatus.Lost
            else -> GameStatus.Playing
        }

        snapshot = snapshot.copy(
            rows = newRows,
            currentInput = "",
            status = newStatus,
        )
        message = GameMessage.None

        if (newStatus != GameStatus.Playing) {
            recordStats(won, submittedGuesses.size)
        }
        if (snapshot.mode == GameMode.Daily) {
            persistence.saveDaily(snapshot)
        }
    }

    private fun recordStats(won: Boolean, guessesUsed: Int) {
        val key = StatsKey(snapshot.wordLength, snapshot.difficulty)
        val current = stats[key] ?: LengthStats(guessDistribution = IntArray(snapshot.maxGuesses))
        val updated = StatsUpdater.recordResult(
            current.withDistributionSize(snapshot.maxGuesses),
            won,
            guessesUsed,
            snapshot.maxGuesses,
            isDaily = snapshot.mode == GameMode.Daily,
        )
        stats = stats + (key to updated)
        persistence.saveStats(stats)
    }

    fun startNextFreeGame() {
        if (snapshot.mode != GameMode.Free) return
        snapshot = createInitialSnapshot(
            GameMode.Free,
            snapshot.wordLength,
            snapshot.difficulty,
            settings,
        )
    }

    fun requestModeChange(mode: GameMode) {
        if (mode == snapshot.mode) return
        requestChange(PendingChange(mode, snapshot.wordLength, snapshot.difficulty))
    }

    fun requestLengthChange(length: Int) {
        val clamped = length.coerceIn(5, 10)
        if (clamped == snapshot.wordLength) return
        requestChange(PendingChange(snapshot.mode, clamped, snapshot.difficulty))
    }

    fun requestDifficultyChange(difficulty: Difficulty) {
        if (difficulty == snapshot.difficulty) return
        requestChange(PendingChange(snapshot.mode, snapshot.wordLength, difficulty))
    }

    private fun requestChange(change: PendingChange) {
        if (hasActiveBoard()) {
            pendingConfirm = change
        } else {
            applyChange(change)
        }
    }

    fun confirmPendingChange() {
        val pending = pendingConfirm ?: return
        pendingConfirm = null
        applyChange(pending)
    }

    fun cancelPendingChange() {
        pendingConfirm = null
    }

    private fun hasActiveBoard(): Boolean =
        submittedGuesses.isNotEmpty() && snapshot.status == GameStatus.Playing

    private fun applyChange(change: PendingChange) {
        settings = settings.copy(
            lastMode = change.mode,
            lastWordLength = change.length,
            lastDifficulty = change.difficulty,
        )
        persistence.saveSettings(settings)
        snapshot = createInitialSnapshot(change.mode, change.length, change.difficulty, settings)
        services.routes?.navigate(
            WebRouteParser.Route(change.mode, change.length, change.difficulty),
        )
    }

    fun updateHardMode(enabled: Boolean) {
        settings = settings.copy(hardMode = enabled)
        snapshot = snapshot.copy(hardMode = enabled)
        persistence.saveSettings(settings)
    }

    fun updateColorblind(enabled: Boolean) {
        settings = settings.copy(colorblind = enabled)
        persistence.saveSettings(settings)
    }

    fun dismissHelp() {
        showHelp = false
        if (!settings.hasSeenHelp) {
            settings = settings.copy(hasSeenHelp = true)
            persistence.saveSettings(settings)
        }
    }

    fun openHelp() { showHelp = true }
    fun openStats() { showStats = true }
    fun closeStats() { showStats = false }
    fun openSettings() { showSettings = true }
    fun closeSettings() { showSettings = false }
    fun openNextPuzzleCountdown() { showNextPuzzleCountdown = true }
    fun closeNextPuzzleCountdown() { showNextPuzzleCountdown = false }

    fun timeUntilNextDailyPuzzle() = timeUntilNextPuzzle(services.clock.now())

    fun copyResult() {
        val text = ShareTextFormatter.format(snapshot, submittedMarks, settings.colorblind)
        services.clipboard.copy(text)
        message = GameMessage.Copied
    }

    fun clearMessage() {
        message = GameMessage.None
    }

    fun currentLengthStats(): LengthStats =
        stats[StatsKey(snapshot.wordLength, snapshot.difficulty)]
            ?.withDistributionSize(snapshot.maxGuesses)
            ?: LengthStats(guessDistribution = IntArray(snapshot.maxGuesses))
}

data class PendingChange(val mode: GameMode, val length: Int, val difficulty: Difficulty)
