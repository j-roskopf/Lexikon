package com.joetr.lexikon.ui.game

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import androidx.compose.ui.unit.dp

class GameLayoutTest {
    @Test
    fun wideScreenKeepsDefaultTileSizeForFiveLetters() {
        val spec = computeGameLayoutSpec(
            maxWidth = 560.dp,
            maxHeight = 900.dp,
            wordLength = 5,
            rowCount = 6,
        )
        assertEquals(58.dp, spec.tileSize)
        assertEquals(52.dp, spec.keyHeight)
        assertEquals(false, spec.scrollContent)
    }

    @Test
    fun narrowWidthShrinksTilesForTenLetters() {
        val spec = computeGameLayoutSpec(
            maxWidth = 360.dp,
            maxHeight = 800.dp,
            wordLength = 10,
            rowCount = 11,
        )
        assertTrue(spec.tileSize < 46.dp)
        assertTrue(spec.tileSize >= 24.dp)
    }

    @Test
    fun wideViewportKeepsControlsOnASingleRail() {
        val spec = computeGameLayoutSpec(
            maxWidth = 560.dp,
            maxHeight = 900.dp,
            wordLength = 5,
            rowCount = 6,
        )
        assertEquals(false, spec.splitControls)
    }

    @Test
    fun narrowViewportSplitsControlsOntoTwoRails() {
        val spec = computeGameLayoutSpec(
            maxWidth = 390.dp,
            maxHeight = 844.dp,
            wordLength = 5,
            rowCount = 6,
        )
        assertEquals(true, spec.splitControls)
    }

    @Test
    fun shortViewportPrioritizesKeyboardAndEnablesScroll() {
        val spec = computeGameLayoutSpec(
            maxWidth = 420.dp,
            maxHeight = 480.dp,
            wordLength = 10,
            rowCount = 11,
        )
        assertEquals(40.dp, spec.keyHeight)
        assertEquals(24.dp, spec.tileSize)
        assertTrue(spec.scrollContent)
    }
}
