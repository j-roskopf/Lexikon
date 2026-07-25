package com.joetr.lexikon.web

import com.joetr.lexikon.ClipboardService
import com.joetr.lexikon.ClockService
import com.joetr.lexikon.LexikonServices
import com.joetr.lexikon.StorageService
import com.joetr.lexikon.WebRouteController
import com.joetr.lexikon.domain.EASTERN_TIME_ZONE
import com.joetr.lexikon.domain.WebRouteParser
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.datetime.LocalDate
import kotlin.time.Clock
import kotlinx.datetime.toLocalDateTime

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
fun createWebServices(): LexikonServices = LexikonServices(
    storage = object : StorageService {
        override fun read(key: String): String? = localStorage.getItem(key)
        override fun write(key: String, value: String) { localStorage.setItem(key, value) }
        override fun remove(key: String) { localStorage.removeItem(key) }
    },
    clipboard = object : ClipboardService {
        override fun copy(text: String) {
            window.navigator.clipboard.writeText(text)
        }
    },
    clock = object : ClockService {
        override fun today(): LocalDate =
            Clock.System.now().toLocalDateTime(EASTERN_TIME_ZONE).date
        override fun now() = Clock.System.now()
    },
    routes = object : WebRouteController {
        override fun navigate(route: WebRouteParser.Route) {
            val path = WebRouteParser.toPath(route.mode, route.length, route.difficulty)
            window.history.pushState(null, "", path)
        }
        override fun currentPath(): String = window.location.pathname
    },
)
