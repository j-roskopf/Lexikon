package com.joetr.lexikon.data

import com.joetr.lexikon.StorageService
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

class PersistenceRepository(private val storage: StorageService) {
    fun loadSettings(): PlayerSettings {
        val raw = storage.read(StorageKeys.SETTINGS) ?: return PlayerSettings()
        return runCatching {
            val dto = LexikonJson.instance.decodeFromString<PlayerSettingsDto>(raw)
            PlayerSettings(
                hardMode = dto.hardMode,
                colorblind = dto.colorblind,
                lastWordLength = dto.lastWordLength.coerceIn(5, 10),
                lastMode = if (dto.lastMode == "Free") GameMode.Free else GameMode.Daily,
                lastDifficulty = Difficulty.fromSlug(dto.lastDifficulty),
                hasSeenHelp = dto.hasSeenHelp,
            )
        }.getOrDefault(PlayerSettings())
    }

    fun saveSettings(settings: PlayerSettings) {
        val dto = PlayerSettingsDto(
            hardMode = settings.hardMode,
            colorblind = settings.colorblind,
            lastWordLength = settings.lastWordLength,
            lastMode = settings.lastMode.name,
            lastDifficulty = settings.lastDifficulty.name,
            hasSeenHelp = settings.hasSeenHelp,
        )
        storage.write(StorageKeys.SETTINGS, LexikonJson.instance.encodeToString(dto))
    }

    fun loadStats(): Map<StatsKey, LengthStats> {
        storage.read(StorageKeys.STATS)?.let { raw ->
            runCatching { decodeStats(raw) { parseStatsKey(it) } }
                .getOrNull()
                ?.let { return it }
        }
        // No v2 stats yet: fold any v1 history into the default difficulty.
        val legacy = storage.read(StorageKeys.STATS_V1) ?: return emptyMap()
        return runCatching {
            decodeStats(legacy) { key ->
                key.toIntOrNull()?.let { StatsKey(it, Difficulty.Default) }
            }
        }.getOrDefault(emptyMap())
    }

    fun saveStats(stats: Map<StatsKey, LengthStats>) {
        val dto = stats
            .mapKeys { (key, _) -> "${key.length}|${key.difficulty.name}" }
            .mapValues { (_, s) ->
                LengthStatsDto(
                    played = s.played,
                    wins = s.wins,
                    currentStreak = s.currentStreak,
                    maxStreak = s.maxStreak,
                    guessDistribution = s.guessDistribution.toList(),
                )
            }
        storage.write(StorageKeys.STATS, LexikonJson.instance.encodeToString(dto))
    }

    private fun decodeStats(
        raw: String,
        toKey: (String) -> StatsKey?,
    ): Map<StatsKey, LengthStats> {
        val map = LexikonJson.instance.decodeFromString<Map<String, LengthStatsDto>>(raw)
        return map.entries.mapNotNull { (key, dto) ->
            toKey(key)?.let { statsKey ->
                statsKey to LengthStats(
                    played = dto.played,
                    wins = dto.wins,
                    currentStreak = dto.currentStreak,
                    maxStreak = dto.maxStreak,
                    guessDistribution = dto.guessDistribution.toIntArray(),
                )
            }
        }.toMap()
    }

    private fun parseStatsKey(key: String): StatsKey? {
        val parts = key.split("|")
        if (parts.size != 2) return null
        val length = parts[0].toIntOrNull() ?: return null
        return StatsKey(length, Difficulty.fromSlug(parts[1]))
    }

    fun loadDaily(length: Int, difficulty: Difficulty, date: String): GameSnapshot? {
        val raw = storage.read(StorageKeys.dailyKey(length, difficulty.slug, date)) ?: return null
        return runCatching { decodeSnapshot(raw) }.getOrNull()
    }

    fun saveDaily(snapshot: GameSnapshot) {
        val date = snapshot.dailyDateUtc ?: return
        storage.write(
            StorageKeys.dailyKey(snapshot.wordLength, snapshot.difficulty.slug, date),
            encodeSnapshot(snapshot),
        )
    }

    private fun encodeSnapshot(snapshot: GameSnapshot): String {
        val dto = GameSnapshotDto(
            mode = snapshot.mode.name,
            wordLength = snapshot.wordLength,
            difficulty = snapshot.difficulty.name,
            maxGuesses = snapshot.maxGuesses,
            answer = snapshot.answer,
            rows = snapshot.rows.map { row ->
                row.tiles.map { tile ->
                    TileDto(tile.char?.toString(), tile.mark.name)
                }
            },
            currentInput = snapshot.currentInput,
            status = snapshot.status.name,
            hardMode = snapshot.hardMode,
            dailyDateUtc = snapshot.dailyDateUtc,
        )
        return LexikonJson.instance.encodeToString(dto)
    }

    private fun decodeSnapshot(raw: String): GameSnapshot {
        val dto = LexikonJson.instance.decodeFromString<GameSnapshotDto>(raw)
        return GameSnapshot(
            mode = if (dto.mode == "Free") GameMode.Free else GameMode.Daily,
            wordLength = dto.wordLength,
            difficulty = Difficulty.fromSlug(dto.difficulty),
            maxGuesses = dto.maxGuesses,
            answer = dto.answer,
            rows = dto.rows.map { tiles ->
                GuessRow(tiles.map { t ->
                    Tile(
                        char = t.char?.firstOrNull(),
                        mark = LetterMark.valueOf(t.mark),
                    )
                })
            },
            currentInput = dto.currentInput,
            status = GameStatus.valueOf(dto.status),
            hardMode = dto.hardMode,
            dailyDateUtc = dto.dailyDateUtc,
        )
    }
}
