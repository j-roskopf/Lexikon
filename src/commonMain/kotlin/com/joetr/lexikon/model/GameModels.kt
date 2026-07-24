package com.joetr.lexikon.model

enum class GameMode { Daily, Free }

enum class LetterMark { Correct, Present, Absent, Empty, Tbd }

data class Tile(val char: Char?, val mark: LetterMark)

data class GuessRow(val tiles: List<Tile>) {
    init {
        require(tiles.isNotEmpty())
    }
}

enum class GameStatus { Playing, Won, Lost }

data class GameSnapshot(
    val mode: GameMode,
    val wordLength: Int,
    val maxGuesses: Int,
    val answer: String,
    val rows: List<GuessRow>,
    val currentInput: String,
    val status: GameStatus,
    val hardMode: Boolean,
    val dailyDateUtc: String?,
)

data class LengthStats(
    val played: Int = 0,
    val wins: Int = 0,
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val guessDistribution: IntArray = IntArray(0),
) {
    fun withDistributionSize(maxGuesses: Int): LengthStats =
        if (guessDistribution.size == maxGuesses) this
        else copy(guessDistribution = guessDistribution.copyOf(maxGuesses))

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LengthStats) return false
        return played == other.played &&
            wins == other.wins &&
            currentStreak == other.currentStreak &&
            maxStreak == other.maxStreak &&
            guessDistribution.contentEquals(other.guessDistribution)
    }

    override fun hashCode(): Int {
        var result = played
        result = 31 * result + wins
        result = 31 * result + currentStreak
        result = 31 * result + maxStreak
        result = 31 * result + guessDistribution.contentHashCode()
        return result
    }
}

data class PlayerSettings(
    val hardMode: Boolean = false,
    val colorblind: Boolean = false,
    val lastWordLength: Int = 5,
    val lastMode: GameMode = GameMode.Daily,
    val hasSeenHelp: Boolean = false,
)
