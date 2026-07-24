package com.joetr.lexikon.domain

import com.joetr.lexikon.model.LetterMark

object GuessEngine {
    fun evaluate(answer: String, guess: String): List<LetterMark> {
        require(answer.length == guess.length) { "Answer and guess must be same length" }
        val n = answer.length
        val marks = MutableList(n) { LetterMark.Absent }
        val remaining = IntArray(26)

        for (i in 0 until n) {
            val c = answer[i]
            if (guess[i] == c) {
                marks[i] = LetterMark.Correct
            } else {
                remaining[c.code - 'A'.code]++
            }
        }

        for (i in 0 until n) {
            if (marks[i] == LetterMark.Correct) continue
            val g = guess[i]
            val idx = g.code - 'A'.code
            if (idx in remaining.indices && remaining[idx] > 0) {
                marks[i] = LetterMark.Present
                remaining[idx]--
            }
        }
        return marks
    }

    fun keyboardMarks(rows: List<List<LetterMark>>, guesses: List<String>): Map<Char, LetterMark> {
        val result = mutableMapOf<Char, LetterMark>()
        for ((rowIndex, marks) in rows.withIndex()) {
            val guess = guesses[rowIndex]
            for (i in marks.indices) {
                val letter = guess[i]
                val mark = marks[i]
                val existing = result[letter]
                result[letter] = when {
                    existing == LetterMark.Correct || mark == LetterMark.Correct -> LetterMark.Correct
                    existing == LetterMark.Present || mark == LetterMark.Present -> LetterMark.Present
                    existing == LetterMark.Absent && mark == LetterMark.Absent -> LetterMark.Absent
                    existing == null -> mark
                    else -> existing
                }
            }
        }
        return result
    }

    fun isHardModeValid(
        answer: String,
        previousGuesses: List<String>,
        previousMarks: List<List<LetterMark>>,
        newGuess: String,
    ): Boolean {
        if (previousGuesses.isEmpty()) return true
        for (i in previousGuesses.indices) {
            val marks = previousMarks[i]
            val prev = previousGuesses[i]
            for (j in marks.indices) {
                when (marks[j]) {
                    LetterMark.Correct -> if (newGuess[j] != prev[j]) return false
                    else -> Unit
                }
            }
        }
        val minRequired = mutableMapOf<Char, Int>()
        for (i in previousGuesses.indices) {
            val marks = previousMarks[i]
            val prev = previousGuesses[i]
            val guessCounts = mutableMapOf<Char, Int>()
            for (j in marks.indices) {
                if (marks[j] == LetterMark.Correct || marks[j] == LetterMark.Present) {
                    guessCounts[prev[j]] = (guessCounts[prev[j]] ?: 0) + 1
                }
            }
            for ((char, count) in guessCounts) {
                minRequired[char] = maxOf(minRequired[char] ?: 0, count)
            }
        }
        for ((letter, count) in minRequired) {
            val satisfied = newGuess.count { it == letter }
            if (satisfied < count) return false
        }
        return true
    }
}
