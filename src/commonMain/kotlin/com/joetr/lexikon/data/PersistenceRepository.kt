package com.joetr.lexikon.data

import com.joetr.lexikon.StorageService
import com.joetr.lexikon.model.GameMode
import com.joetr.lexikon.model.GameSnapshot
import com.joetr.lexikon.model.GameStatus
import com.joetr.lexikon.model.GuessRow
import com.joetr.lexikon.model.LengthStats
import com.joetr.lexikon.model.LetterMark
import com.joetr.lexikon.model.PlayerSettings
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
            hasSeenHelp = settings.hasSeenHelp,
        )
        storage.write(StorageKeys.SETTINGS, LexikonJson.instance.encodeToString(dto))
    }

    fun loadStats(): Map<Int, LengthStats> {
        val raw = storage.read(StorageKeys.STATS) ?: return emptyMap()
        return runCatching {
            val map = LexikonJson.instance.decodeFromString<Map<String, LengthStatsDto>>(raw)
            map.mapKeys { it.key.toInt() }.mapValues { (_, dto) ->
                LengthStats(
                    played = dto.played,
                    wins = dto.wins,
                    currentStreak = dto.currentStreak,
                    maxStreak = dto.maxStreak,
                    guessDistribution = dto.guessDistribution.toIntArray(),
                )
            }
        }.getOrDefault(emptyMap())
    }

    fun saveStats(stats: Map<Int, LengthStats>) {
        val dto = stats.mapKeys { it.key.toString() }.mapValues { (_, s) ->
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

    fun loadDaily(length: Int, date: String): GameSnapshot? {
        val raw = storage.read(StorageKeys.dailyKey(length, date)) ?: return null
        return runCatching { decodeSnapshot(raw) }.getOrNull()
    }

    fun saveDaily(snapshot: GameSnapshot) {
        val date = snapshot.dailyDateUtc ?: return
        storage.write(StorageKeys.dailyKey(snapshot.wordLength, date), encodeSnapshot(snapshot))
    }

    private fun encodeSnapshot(snapshot: GameSnapshot): String {
        val dto = GameSnapshotDto(
            mode = snapshot.mode.name,
            wordLength = snapshot.wordLength,
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
