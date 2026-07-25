package com.joetr.lexikon.domain

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * The daily puzzle rolls over at midnight US Eastern time, not UTC.
 */
val EASTERN_TIME_ZONE: TimeZone = TimeZone.of("America/New_York")

/**
 * Time remaining until the next daily puzzle unlocks (next midnight Eastern).
 */
fun timeUntilNextPuzzle(now: Instant): Duration {
    val today = now.toLocalDateTime(EASTERN_TIME_ZONE).date
    val tomorrow = today.plus(1, kotlinx.datetime.DateTimeUnit.DAY)
    val nextMidnight = tomorrow.atStartOfDayIn(EASTERN_TIME_ZONE)
    return nextMidnight - now
}
