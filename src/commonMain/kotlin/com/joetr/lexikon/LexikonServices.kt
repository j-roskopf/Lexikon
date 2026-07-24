package com.joetr.lexikon

import com.joetr.lexikon.domain.WebRouteParser
import kotlinx.datetime.LocalDate

interface StorageService {
    fun read(key: String): String?
    fun write(key: String, value: String)
    fun remove(key: String)
}

interface ClipboardService {
    fun copy(text: String)
}

interface ClockService {
    fun todayUtc(): LocalDate
}

interface WebRouteController {
    fun navigate(route: WebRouteParser.Route)
    fun currentPath(): String
}

data class LexikonServices(
    val storage: StorageService,
    val clipboard: ClipboardService,
    val clock: ClockService,
    val routes: WebRouteController? = null,
    val disableAnimations: Boolean = false,
    val seededRandom: kotlin.random.Random? = null,
)
