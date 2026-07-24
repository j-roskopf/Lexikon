package com.joetr.lexikon.desktop

import com.joetr.lexikon.ClipboardService
import com.joetr.lexikon.ClockService
import com.joetr.lexikon.LexikonServices
import com.joetr.lexikon.StorageService
import com.joetr.lexikon.WebRouteController
import com.joetr.lexikon.domain.WebRouteParser
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.util.prefs.Preferences
import kotlin.time.Clock

fun createDesktopServices(): LexikonServices {
    val prefs = Preferences.userRoot().node("com.joetr.lexikon")
    return LexikonServices(
        storage = object : StorageService {
            override fun read(key: String): String? = prefs.get(key, null)
            override fun write(key: String, value: String) { prefs.put(key, value); prefs.flush() }
            override fun remove(key: String) { prefs.remove(key); prefs.flush() }
        },
        clipboard = object : ClipboardService {
            override fun copy(text: String) {
                Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
            }
        },
        clock = object : ClockService {
            override fun todayUtc(): LocalDate =
                Clock.System.now().toLocalDateTime(TimeZone.UTC).date
        },
        routes = object : WebRouteController {
            override fun navigate(route: WebRouteParser.Route) = Unit
            override fun currentPath(): String = "/"
        },
    )
}
