package com.joetr.lexikon

import com.joetr.lexikon.domain.WebRouteParser
import kotlinx.datetime.LocalDate
import kotlinx.datetime.atStartOfDayIn
import com.joetr.lexikon.domain.EASTERN_TIME_ZONE
import kotlin.random.Random
import kotlin.time.Instant

fun fakeScreenshotServices(): LexikonServices = LexikonServices(
    storage = InMemoryStorage(),
    clipboard = NoopClipboard(),
    clock = FixedClock(LocalDate(2026, 7, 24)),
    routes = object : WebRouteController {
        override fun navigate(route: WebRouteParser.Route) = Unit
        override fun currentPath(): String = "/daily/5"
    },
    disableAnimations = true,
    seededRandom = Random(42),
)

class InMemoryStorage : StorageService {
    private val data = mutableMapOf<String, String>()
    override fun read(key: String): String? = data[key]
    override fun write(key: String, value: String) { data[key] = value }
    override fun remove(key: String) { data.remove(key) }
}

class NoopClipboard : ClipboardService {
    override fun copy(text: String) = Unit
}

class FixedClock(private val date: LocalDate) : ClockService {
    override fun today(): LocalDate = date
    override fun now(): Instant = date.atStartOfDayIn(EASTERN_TIME_ZONE)
}
