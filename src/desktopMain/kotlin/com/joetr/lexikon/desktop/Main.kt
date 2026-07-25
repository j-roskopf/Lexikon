package com.joetr.lexikon.desktop

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.joetr.lexikon.LexikonApp
import com.joetr.lexikon.LexikonServices
import com.joetr.lexikon.domain.WebRouteParser
import java.awt.Dimension

fun main() {
    val services = createDesktopServices()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Lexikon",
            state = rememberWindowState(width = 580.dp, height = 900.dp),
        ) {
            window.minimumSize = Dimension(580, 900)
            LexikonApp(
                services = services,
                initialRoute = services.storage.let {
                    val settings = com.joetr.lexikon.data.PersistenceRepository(it).loadSettings()
                    WebRouteParser.Route(
                        com.joetr.lexikon.model.GameMode.Daily,
                        settings.lastWordLength,
                        settings.lastDifficulty,
                    )
                },
            )
        }
    }
}
