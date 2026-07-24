package com.joetr.lexikon.domain

import com.joetr.lexikon.model.LetterMark
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GuessEngineTest {
    @Test
    fun duplicateLetterScoring() {
        assertEquals(
            listOf(LetterMark.Correct, LetterMark.Correct, LetterMark.Absent, LetterMark.Absent, LetterMark.Absent),
            GuessEngine.evaluate("ABBEY", "ABACK"),
        )
        assertEquals(
            listOf(LetterMark.Present, LetterMark.Present, LetterMark.Correct, LetterMark.Correct, LetterMark.Absent),
            GuessEngine.evaluate("ABBEY", "BABES"),
        )
        assertEquals(
            listOf(LetterMark.Correct, LetterMark.Correct, LetterMark.Absent, LetterMark.Present, LetterMark.Present),
            GuessEngine.evaluate("SPEED", "SPADE"),
        )
    }

    @Test
    fun hardModeRequiresCorrectPositions() {
        val marks = listOf(LetterMark.Absent, LetterMark.Absent, LetterMark.Correct, LetterMark.Absent, LetterMark.Absent)
        assertTrue(
            !GuessEngine.isHardModeValid("CRANE", listOf("SLATE"), listOf(marks), "BROKE"),
        )
        assertTrue(
            GuessEngine.isHardModeValid("CRANE", listOf("SLATE"), listOf(marks), "GRADE"),
        )
    }

    @Test
    fun hardModeReusesPresentLettersAcrossMultipleGuesses() {
        val marks1 = listOf(LetterMark.Present, LetterMark.Absent, LetterMark.Absent, LetterMark.Absent, LetterMark.Absent) // 'A' present in guess 1
        val marks2 = listOf(LetterMark.Absent, LetterMark.Present, LetterMark.Absent, LetterMark.Absent, LetterMark.Absent) // 'A' present in guess 2
        
        // Both guess 1 and guess 2 revealed 1 'A' present. Guess 3 has 1 'A' -> valid (should NOT require 2 'A's).
        assertTrue(
            GuessEngine.isHardModeValid("CRANE", listOf("AUDIO", "STARE"), listOf(marks1, marks2), "PLANT"),
        )
        // Guess 3 without 'A' -> invalid
        assertTrue(
            !GuessEngine.isHardModeValid("CRANE", listOf("AUDIO", "STARE"), listOf(marks1, marks2), "PLENT"),
        )
    }

    @Test
    fun keyboardAggregateSeverity() {
        val marks = listOf(
            listOf(LetterMark.Absent, LetterMark.Present, LetterMark.Absent, LetterMark.Absent, LetterMark.Absent),
            listOf(LetterMark.Correct, LetterMark.Absent, LetterMark.Absent, LetterMark.Absent, LetterMark.Absent),
        )
        val kb = GuessEngine.keyboardMarks(marks, listOf("APPLE", "ABOUT"))
        assertEquals(LetterMark.Correct, kb['A'])
        assertEquals(LetterMark.Present, kb['P'])
    }
}
