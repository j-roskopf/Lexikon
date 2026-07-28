package com.joetr.lexikon

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.RoborazziOptions
import io.github.takahirom.roborazzi.captureRoboImage
import com.joetr.lexikon.data.PersistenceRepository
import com.joetr.lexikon.domain.DictionaryRepository
import com.joetr.lexikon.domain.GameController
import com.joetr.lexikon.domain.WebRouteParser
import com.joetr.lexikon.model.Difficulty
import com.joetr.lexikon.model.GameMode
import com.joetr.lexikon.model.PlayerSettings
import com.joetr.lexikon.ui.shell.LexikonShell
import com.joetr.lexikon.ui.theme.LexikonTheme
import kotlin.test.Test

class LexikonDesktopScreenshotTest {
    private val screenshotOptions = RoborazziOptions(
        compareOptions = RoborazziOptions.CompareOptions(changeThreshold = 0.001f),
    )

    @OptIn(ExperimentalTestApi::class, ExperimentalComposeUiApi::class)
    @Test
    fun desktopSurfaces() {
        data class Scenario(
            val name: String,
            val colorblind: Boolean = false,
            val showHelp: Boolean = false,
            val wordLength: Int = 5,
            val width: Int = 420,
            val height: Int = 800,
            val guesses: List<String> = emptyList(),
        )

        listOf(
            Scenario("empty-daily-5"),
            Scenario("compact-ten-letters", wordLength = 10),
            Scenario("compact-viewport", wordLength = 10, width = 360, height = 640),
            Scenario("colorblind", colorblind = true),
            Scenario("help-dialog", showHelp = true),
            Scenario("wide-viewport", width = 640, height = 900),
            Scenario("won-state", guesses = listOf("crane")),
            Scenario("lost-state", guesses = List(6) { "slate" }),
        ).forEach { scenario ->
            runDesktopComposeUiTest(width = scenario.width, height = scenario.height) {
                setContent {
                    val controller = remember(scenario) { screenshotController(scenario.colorblind, scenario.wordLength) }
                    if (scenario.showHelp) {
                        LaunchedEffect(Unit) { controller.openHelp() }
                    }
                    if (scenario.guesses.isNotEmpty()) {
                        LaunchedEffect(Unit) {
                            scenario.guesses.forEach { guess ->
                                guess.forEach { controller.type(it) }
                                controller.submit()
                            }
                        }
                    }
                    LexikonTheme(colorblind = controller.settings.colorblind) {
                        Box(Modifier.size(scenario.width.dp, scenario.height.dp)) {
                            LexikonShell(controller, disableAnimations = true)
                        }
                    }
                }
                waitForIdle()
                onRoot().captureRoboImage(
                    filePath = "src/screenshotTest/roborazzi/${baselinePlatform()}/${scenario.name}.png",
                    roborazziOptions = screenshotOptions,
                )
            }
        }
    }

    private fun screenshotController(colorblind: Boolean = false, wordLength: Int = 5): GameController {
        val services = fakeScreenshotServices()
        val sampleWord = when (wordLength) {
            10 -> "background"
            else -> "crane"
        }
        val dictionary = DictionaryRepository.fromWordLists(
            mapOf(wordLength to Difficulty.entries.associateWith { listOf(sampleWord) }),
            mapOf(wordLength to listOf(sampleWord, "slate")),
        )
        val persistence = PersistenceRepository(services.storage)
        val settings = PlayerSettings(colorblind = colorblind, hasSeenHelp = true)
        return GameController(
            services,
            dictionary,
            persistence,
            settings,
            WebRouteParser.Route(GameMode.Daily, wordLength),
        )
    }

    private fun baselinePlatform(): String = when {
        System.getProperty("os.name").contains("mac", ignoreCase = true) -> "macos"
        System.getProperty("os.name").contains("windows", ignoreCase = true) -> "windows"
        else -> "linux"
    }
}
