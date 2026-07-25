package com.joetr.lexikon.domain

import com.joetr.lexikon.model.Difficulty
import com.joetr.lexikon.model.GameMode
import com.joetr.lexikon.model.GameStatus
import com.joetr.lexikon.model.LengthStats
import com.joetr.lexikon.model.LetterMark
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DailyPuzzleSelectorTest {
    @Test
    fun stableHashForFixedDate() {
        val answers = listOf("ALPHA", "BRAVO", "CRANE", "DELTA", "EAGLE")
        val a = DailyPuzzleSelector.dailyAnswer(LocalDate(2026, 7, 24), 5, Difficulty.Medium, answers)
        val b = DailyPuzzleSelector.dailyAnswer(LocalDate(2026, 7, 24), 5, Difficulty.Medium, answers)
        assertEquals(a, b)
    }

    @Test
    fun difficultiesGetIndependentAnswers() {
        // Same pool and date for every tier, so any difference comes from the seed alone.
        val answers = (1..50).map { "WORD${it.toString().padStart(2, '0')}" }
        val date = LocalDate(2026, 7, 24)
        val picks = Difficulty.entries.map {
            DailyPuzzleSelector.dailyAnswer(date, 5, it, answers)
        }
        assertEquals(picks.size, picks.toSet().size)
    }

    @Test
    fun differentLengthsDifferentAnswers() {
        val answers5 = (1..20).map { "WORD${it.toString().padStart(1, '0')}".take(5).uppercase() }
        val answers6 = (1..20).map { "WORDS${it.toString().padStart(1, '0')}".take(6).uppercase() }
        val date = LocalDate(2026, 7, 24)
        val a5 = DailyPuzzleSelector.dailyAnswer(date, 5, Difficulty.Medium, answers5)
        val a6 = DailyPuzzleSelector.dailyAnswer(date, 6, Difficulty.Medium, answers6)
        assertTrue(a5.length == 5)
        assertTrue(a6.length == 6)
    }

    @Test
    fun seedFormat() {
        assertEquals(
            "lexikon|2026-07-24|5|hard",
            DailyPuzzleSelector.seedString(LocalDate(2026, 7, 24), 5, Difficulty.Hard),
        )
    }
}

class StatsUpdaterTest {
    @Test
    fun dailyStreakOnlyOnDailyWins() {
        val base = LengthStats(guessDistribution = IntArray(6))
        val afterFreeWin = StatsUpdater.recordResult(base, won = true, guessesUsed = 3, maxGuesses = 6, isDaily = false)
        assertEquals(0, afterFreeWin.currentStreak)
        val afterDailyWin = StatsUpdater.recordResult(afterFreeWin, won = true, guessesUsed = 2, maxGuesses = 6, isDaily = true)
        assertEquals(1, afterDailyWin.currentStreak)
        val afterDailyLoss = StatsUpdater.recordResult(afterDailyWin, won = false, guessesUsed = 6, maxGuesses = 6, isDaily = true)
        assertEquals(0, afterDailyLoss.currentStreak)
    }

    @Test
    fun guessBudgetIsLengthPlusOne() {
        assertEquals(6, maxGuessesForLength(5))
        assertEquals(11, maxGuessesForLength(10))
    }
}

class ShareTextFormatterTest {
    @Test
    fun formatsDailyResult() {
        val snap = com.joetr.lexikon.model.GameSnapshot(
            mode = GameMode.Daily,
            wordLength = 5,
            difficulty = Difficulty.Hard,
            maxGuesses = 6,
            answer = "CRANE",
            rows = emptyList(),
            currentInput = "",
            status = GameStatus.Won,
            hardMode = false,
            dailyDateUtc = "2026-07-24",
        )
        val text = ShareTextFormatter.format(
            snap,
            listOf(listOf(LetterMark.Absent, LetterMark.Present, LetterMark.Absent, LetterMark.Absent, LetterMark.Absent)),
            colorblind = false,
        )
        assertTrue(text.startsWith("Lexikon 5 Hard 1/6"))
        assertTrue(text.contains("2026-07-24"))
    }
}

class WebRouteParserTest {
    @Test
    fun parsesRoutes() {
        assertEquals(WebRouteParser.Route(GameMode.Daily, 5), WebRouteParser.parse("/"))
        assertEquals(WebRouteParser.Route(GameMode.Daily, 8), WebRouteParser.parse("/daily/8"))
        assertEquals(WebRouteParser.Route(GameMode.Free, 8), WebRouteParser.parse("/free/8"))
        assertEquals(WebRouteParser.Route(GameMode.Daily, 5), WebRouteParser.parse("/invalid/path"))
    }

    @Test
    fun parsesDifficultySegment() {
        assertEquals(
            WebRouteParser.Route(GameMode.Daily, 8, Difficulty.Hard),
            WebRouteParser.parse("/daily/8/hard"),
        )
        assertEquals(
            WebRouteParser.Route(GameMode.Free, 5, Difficulty.Easy),
            WebRouteParser.parse("/free/5/easy"),
        )
        // Unknown tier falls back to the default rather than failing the route.
        assertEquals(
            WebRouteParser.Route(GameMode.Daily, 8, Difficulty.Default),
            WebRouteParser.parse("/daily/8/bogus"),
        )
    }

    @Test
    fun buildsPaths() {
        assertEquals("/", WebRouteParser.toPath(GameMode.Daily, 5, Difficulty.Default))
        assertEquals("/daily/5/hard", WebRouteParser.toPath(GameMode.Daily, 5, Difficulty.Hard))
        assertEquals("/free/8", WebRouteParser.toPath(GameMode.Free, 8, Difficulty.Default))
        assertEquals("/free/8/easy", WebRouteParser.toPath(GameMode.Free, 8, Difficulty.Easy))
    }

    @Test
    fun pathsRoundTrip() {
        for (mode in GameMode.entries) {
            for (length in 5..10) {
                for (difficulty in Difficulty.entries) {
                    val path = WebRouteParser.toPath(mode, length, difficulty)
                    assertEquals(WebRouteParser.Route(mode, length, difficulty), WebRouteParser.parse(path))
                }
            }
        }
    }
}

class DictionaryRepositoryTest {
    private fun repo() = DictionaryRepository.fromWordLists(
        mapOf(
            5 to mapOf(
                Difficulty.Easy to listOf("crane"),
                Difficulty.Medium to listOf("slate"),
                Difficulty.Hard to listOf("zesty"),
            ),
        ),
        mapOf(5 to listOf("crane", "slate", "zesty", "audio")),
    )

    @Test
    fun validatesGuesses() {
        assertTrue(repo().isValidGuess(5, "SLATE"))
        assertFalse(repo().isValidGuess(5, "ZZZZZ"))
    }

    @Test
    fun answersAreScopedToDifficulty() {
        val repo = repo()
        assertEquals(setOf("CRANE"), repo.answers(5, Difficulty.Easy))
        assertEquals(setOf("ZESTY"), repo.answers(5, Difficulty.Hard))
        assertEquals(setOf("CRANE", "SLATE", "ZESTY"), repo.allAnswers(5))
    }

    @Test
    fun everyAnswerIsAValidGuess() {
        val repo = repo()
        assertTrue(repo.allAnswers(5).all { repo.isValidGuess(5, it) })
    }
}
