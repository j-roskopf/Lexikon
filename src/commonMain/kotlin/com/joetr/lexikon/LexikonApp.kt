package com.joetr.lexikon

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.joetr.lexikon.data.PersistenceRepository
import com.joetr.lexikon.domain.DictionaryLoader
import com.joetr.lexikon.domain.DictionaryRepository
import com.joetr.lexikon.domain.GameController
import com.joetr.lexikon.domain.WebRouteParser
import com.joetr.lexikon.model.PlayerSettings
import com.joetr.lexikon.ui.shell.LexikonShell
import com.joetr.lexikon.ui.theme.LexikonTheme

@Composable
fun LexikonApp(
    services: LexikonServices,
    initialRoute: WebRouteParser.Route? = null,
    initialSettingsOverride: PlayerSettings? = null,
) {
    var dictionary by remember { mutableStateOf<DictionaryRepository?>(null) }
    LaunchedEffect(Unit) {
        dictionary = DictionaryLoader.load()
    }
    val loaded = dictionary
    if (loaded == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    val persistence = remember { PersistenceRepository(services.storage) }
    val settings = remember { initialSettingsOverride ?: persistence.loadSettings() }
    val route = initialRoute ?: services.routes?.let {
        WebRouteParser.parse(it.currentPath(), settings.lastWordLength, settings.lastDifficulty)
    } ?: WebRouteParser.Route(settings.lastMode, settings.lastWordLength, settings.lastDifficulty)
    val controller = remember(services, loaded, route) {
        GameController(services, loaded, persistence, settings, route)
    }
    LexikonTheme(colorblind = controller.settings.colorblind) {
        LexikonShell(controller, services.disableAnimations)
    }
}
