package com.joetr.lexikon

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.dp
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
import kotlin.test.assertEquals

class LexikonKeyboardInputTest {
    /** The shell has to claim focus on its own, or typing does nothing until the first tap. */
    @OptIn(ExperimentalTestApi::class, ExperimentalComposeUiApi::class)
    @Test
    fun typesWithoutInteractingFirst() {
        runDesktopComposeUiTest(width = 420, height = 800) {
            lateinit var controller: GameController
            setContent {
                controller = remember { keyboardController() }
                LexikonTheme(colorblind = false) {
                    Box(Modifier.size(420.dp, 800.dp)) {
                        LexikonShell(controller, disableAnimations = true)
                    }
                }
            }
            waitForIdle()

            onRoot().performKeyInput {
                pressKey(Key.C)
                pressKey(Key.R)
                pressKey(Key.A)
            }
            waitForIdle()

            assertEquals("CRA", controller.snapshot.currentInput)

            onRoot().performKeyInput {
                pressKey(Key.N)
                pressKey(Key.E)
                pressKey(Key.Enter)
            }
            waitForIdle()

            assertEquals("", controller.snapshot.currentInput)
            assertEquals(
                "CRANE",
                controller.snapshot.rows.first().tiles.mapNotNull { it.char }.joinToString(""),
            )
        }
    }

    private fun keyboardController(): GameController {
        val services = fakeScreenshotServices()
        val dictionary = DictionaryRepository.fromWordLists(
            mapOf(5 to Difficulty.entries.associateWith { listOf("crane") }),
            mapOf(5 to listOf("crane", "slate")),
        )
        return GameController(
            services,
            dictionary,
            PersistenceRepository(services.storage),
            PlayerSettings(hasSeenHelp = true),
            WebRouteParser.Route(GameMode.Daily, 5),
        )
    }
}
