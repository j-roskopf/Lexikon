package com.joetr.lexikon.domain

import com.joetr.lexikon.model.GameMode
import com.joetr.lexikon.model.GameSnapshot
import com.joetr.lexikon.model.GameStatus
import com.joetr.lexikon.model.GuessRow
import com.joetr.lexikon.model.LengthStats
import com.joetr.lexikon.model.LetterMark
import com.joetr.lexikon.model.PlayerSettings
import com.joetr.lexikon.model.Tile
import kotlinx.datetime.LocalDate

fun maxGuessesForLength(length: Int): Int = length + 1

fun emptyRows(wordLength: Int, maxGuesses: Int): List<GuessRow> =
    List(maxGuesses) { GuessRow(List(wordLength) { Tile(null, LetterMark.Empty) }) }

fun createGameSnapshot(
    mode: GameMode,
    wordLength: Int,
    answer: String,
    hardMode: Boolean,
    dailyDateUtc: String?,
): GameSnapshot = GameSnapshot(
    mode = mode,
    wordLength = wordLength,
    maxGuesses = maxGuessesForLength(wordLength),
    answer = answer.uppercase(),
    rows = emptyRows(wordLength, maxGuessesForLength(wordLength)),
    currentInput = "",
    status = GameStatus.Playing,
    hardMode = hardMode,
    dailyDateUtc = dailyDateUtc,
)

object StatsUpdater {
    fun recordResult(
        stats: LengthStats,
        won: Boolean,
        guessesUsed: Int,
        maxGuesses: Int,
        isDaily: Boolean,
    ): LengthStats {
        val dist = stats.withDistributionSize(maxGuesses).guessDistribution.copyOf()
        val played = stats.played + 1
        val wins = stats.wins + if (won) 1 else 0
        val currentStreak = when {
            !isDaily -> stats.currentStreak
            won -> stats.currentStreak + 1
            else -> 0
        }
        val maxStreak = maxOf(stats.maxStreak, currentStreak)
        if (won && guessesUsed in 1..maxGuesses) {
            dist[guessesUsed - 1] = dist[guessesUsed - 1] + 1
        }
        return LengthStats(played, wins, currentStreak, maxStreak, dist)
    }
}

object ShareTextFormatter {
    fun format(
        snapshot: GameSnapshot,
        marks: List<List<LetterMark>>,
        colorblind: Boolean,
    ): String {
        val guessesUsed = marks.size
        val result = when (snapshot.status) {
            GameStatus.Won -> "$guessesUsed/${snapshot.maxGuesses}"
            GameStatus.Lost -> "X/${snapshot.maxGuesses}"
            GameStatus.Playing -> "${guessesUsed}/${snapshot.maxGuesses}"
        }
        val header = "Lexikon ${snapshot.wordLength} $result"
        val dateLine = when (snapshot.mode) {
            GameMode.Daily -> snapshot.dailyDateUtc ?: ""
            GameMode.Free -> "Free play"
        }
        val grid = marks.joinToString("\n") { row ->
            row.joinToString("") { markToEmoji(it, colorblind) }
        }
        return buildString {
            appendLine(header)
            if (dateLine.isNotEmpty()) appendLine(dateLine)
            append(grid)
        }.trimEnd()
    }

    private fun markToEmoji(mark: LetterMark, colorblind: Boolean): String = when (mark) {
        LetterMark.Correct -> if (colorblind) "🟦" else "🟩"
        LetterMark.Present -> if (colorblind) "🟧" else "🟨"
        LetterMark.Absent -> "⬛"
        LetterMark.Empty, LetterMark.Tbd -> "⬜"
    }
}

object WebRouteParser {
    data class Route(val mode: GameMode, val length: Int)

    fun parse(pathname: String, defaultLength: Int = 5): Route {
        val segments = pathname.trim('/').split('/').filter { it.isNotEmpty() }
        return when {
            segments.isEmpty() -> Route(GameMode.Daily, defaultLength.coerceIn(5, 10))
            segments.size == 2 && segments[0] == "daily" -> {
                Route(GameMode.Daily, segments[1].toIntOrNull()?.coerceIn(5, 10) ?: 5)
            }
            segments.size == 2 && segments[0] == "free" -> {
                Route(GameMode.Free, segments[1].toIntOrNull()?.coerceIn(5, 10) ?: 5)
            }
            else -> Route(GameMode.Daily, 5)
        }
    }

    fun toPath(mode: GameMode, length: Int): String = when (mode) {
        GameMode.Daily -> if (length == 5) "/" else "/daily/$length"
        GameMode.Free -> "/free/$length"
    }
}
