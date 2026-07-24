package com.joetr.lexikon.domain

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
        val a = DailyPuzzleSelector.dailyAnswer(LocalDate(2026, 7, 24), 5, answers)
        val b = DailyPuzzleSelector.dailyAnswer(LocalDate(2026, 7, 24), 5, answers)
        assertEquals(a, b)
    }

    @Test
    fun differentLengthsDifferentAnswers() {
        val answers5 = (1..20).map { "WORD${it.toString().padStart(1, '0')}".take(5).uppercase() }
        val answers6 = (1..20).map { "WORDS${it.toString().padStart(1, '0')}".take(6).uppercase() }
        val date = LocalDate(2026, 7, 24)
        val a5 = DailyPuzzleSelector.dailyAnswer(date, 5, answers5)
        val a6 = DailyPuzzleSelector.dailyAnswer(date, 6, answers6)
        assertTrue(a5.length == 5)
        assertTrue(a6.length == 6)
    }

    @Test
    fun seedFormat() {
        assertEquals("lexikon|2026-07-24|5", DailyPuzzleSelector.seedString(LocalDate(2026, 7, 24), 5))
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
        assertTrue(text.startsWith("Lexikon 5 1/6"))
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
}

class DictionaryRepositoryTest {
    @Test
    fun validatesGuesses() {
        val repo = DictionaryRepository.fromWordLists(
            mapOf(5 to listOf("crane")),
            mapOf(5 to listOf("crane", "slate")),
        )
        assertTrue(repo.isValidGuess(5, "SLATE"))
        assertFalse(repo.isValidGuess(5, "ZZZZZ"))
    }
}
