package com.joetr.lexikon.domain

import com.joetr.lexikon.model.Difficulty
import kotlinx.datetime.LocalDate

/**
 * Canonical seed: `lexikon|{yyyy-MM-dd}|{length}|{difficulty}`, where the date is the puzzle day
 * in US Eastern time. Including the difficulty gives each day an independent easy, medium and
 * hard word per length.
 * Hash: SHA-256 of UTF-8 seed, first 8 bytes as unsigned long, mod answer list size.
 */
object DailyPuzzleSelector {
    const val APP_SEED_PREFIX = "lexikon"

    fun seedString(date: LocalDate, length: Int, difficulty: Difficulty): String =
        "$APP_SEED_PREFIX|$date|$length|${difficulty.slug}"

    fun dailyAnswer(
        date: LocalDate,
        length: Int,
        difficulty: Difficulty,
        answers: List<String>,
    ): String {
        require(answers.isNotEmpty()) { "Answer list must not be empty" }
        val seed = seedString(date, length, difficulty)
        val hash = sha256(seed.encodeToByteArray())
        val index = hashToIndex(hash, answers.size)
        return answers[index]
    }

    fun hashToIndex(hash: ByteArray, size: Int): Int {
        require(hash.size >= 8)
        var value = 0L
        for (i in 0 until 8) {
            value = (value shl 8) or (hash[i].toLong() and 0xFF)
        }
        val positive = value and Long.MAX_VALUE
        return (positive % size).toInt()
    }
}
