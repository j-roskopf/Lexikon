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
    const val STATS = "stats.v1"
    fun dailyKey(length: Int, date: String) = "daily.v1.$length.$date"
}
