package com.joetr.lexikon.data

import com.joetr.lexikon.model.GameSnapshot
import com.joetr.lexikon.model.LengthStats
import com.joetr.lexikon.model.PlayerSettings
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class PlayerSettingsDto(
    val hardMode: Boolean = false,
    val colorblind: Boolean = false,
    val lastWordLength: Int = 5,
    val lastMode: String = "Daily",
    val lastDifficulty: String = "Medium",
    val hasSeenHelp: Boolean = false,
)

@Serializable
data class LengthStatsDto(
    val played: Int = 0,
    val wins: Int = 0,
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val guessDistribution: List<Int> = emptyList(),
)

@Serializable
data class GameSnapshotDto(
    val mode: String,
    val wordLength: Int,
    val difficulty: String = "Medium",
    val maxGuesses: Int,
    val answer: String,
    val rows: List<List<TileDto>>,
    val currentInput: String,
    val status: String,
    val hardMode: Boolean,
    val dailyDateUtc: String?,
)

@Serializable
data class TileDto(val char: String?, val mark: String)

object LexikonJson {
    val instance = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
}

object StorageKeys {
    const val SETTINGS = "settings"

    /** v1 was keyed by word length alone; it is migrated into the Medium difficulty. */
    const val STATS_V1 = "stats.v1"
    const val STATS = "stats.v2"

    /** v1 daily keys had no difficulty, so all three tiers would collide on one entry. */
    fun dailyKey(length: Int, difficulty: String, date: String) =
        "daily.v2.$length.${difficulty.lowercase()}.$date"
}
