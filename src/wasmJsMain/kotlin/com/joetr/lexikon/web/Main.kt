package com.joetr.lexikon.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.joetr.lexikon.LexikonApp
import com.joetr.lexikon.domain.WebRouteParser
import kotlinx.browser.document
import kotlinx.browser.window

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val root = document.getElementById("root") ?: error("Missing #root")
    val services = createWebServices()
    val params = window.location.search
    val isE2e = params.contains("e2e=1")
    val isScreenshot = params.contains("screenshot=")
    val disableAnimations = isScreenshot || isE2e
    val route = WebRouteParser.parse(window.location.pathname)
    ComposeViewport(root) {
        LexikonApp(
            services = services.copy(disableAnimations = disableAnimations),
            initialRoute = route,
            initialSettingsOverride = if (isE2e || isScreenshot) {
                com.joetr.lexikon.model.PlayerSettings(hasSeenHelp = true)
            } else {
                null
            },
        )
    }
    installKeyboardBridge()
}
