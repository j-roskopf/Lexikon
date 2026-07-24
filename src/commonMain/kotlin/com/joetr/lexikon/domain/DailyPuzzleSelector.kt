package com.joetr.lexikon.domain

import kotlinx.datetime.LocalDate

/**
 * Canonical seed: `lexikon|{yyyy-MM-dd}|{length}`
 * Hash: SHA-256 of UTF-8 seed, first 8 bytes as unsigned long, mod answer list size.
 */
object DailyPuzzleSelector {
    const val APP_SEED_PREFIX = "lexikon"

    fun seedString(dateUtc: LocalDate, length: Int): String =
        "$APP_SEED_PREFIX|$dateUtc|$length"

    fun dailyAnswer(dateUtc: LocalDate, length: Int, answers: List<String>): String {
        require(answers.isNotEmpty()) { "Answer list must not be empty" }
        val seed = seedString(dateUtc, length)
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
