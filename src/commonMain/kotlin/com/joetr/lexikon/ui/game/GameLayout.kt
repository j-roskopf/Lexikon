package com.joetr.lexikon.ui.game

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class GameLayoutSpec(
    val tileSize: Dp,
    val tileGap: Dp,
    val keyHeight: Dp,
    val keyboardHeight: Dp,
    val mastheadHeight: Dp,
    val railHeight: Dp,
    val railGap: Dp,
    val compactHeader: Boolean,
    val splitControls: Boolean,
    val scrollContent: Boolean,
)

/** Second rail only appears when the three control groups cannot share one line. */
private val SINGLE_RAIL_MIN_WIDTH = 516.dp
private val SPLIT_RAIL_GAP = 6.dp

/**
 * [maxWidth] / [maxHeight] come from the BoxWithConstraints that already sits inside the
 * shell's padding, so they are the usable content box. Do not subtract padding again.
 */
internal fun computeGameLayoutSpec(
    maxWidth: Dp,
    maxHeight: Dp,
    wordLength: Int,
    rowCount: Int,
): GameLayoutSpec {
    val contentWidth = maxWidth

    val compactHeader = maxHeight < 640.dp || maxWidth < 360.dp
    val splitControls = contentWidth < SINGLE_RAIL_MIN_WIDTH

    // Wordmark row plus one or two control rails. Keep in sync with Header.
    val mastheadHeight = if (compactHeader) 38.dp else 44.dp
    val railHeight = if (compactHeader) 30.dp else 34.dp
    val railGap = if (compactHeader) 8.dp else 10.dp
    val headerHeight = mastheadHeight + railGap + railHeight +
        if (splitControls) SPLIT_RAIL_GAP + railHeight else 0.dp
    val contentGaps = 12.dp

    val maxTileSize = when {
        wordLength <= 5 -> 58.dp
        wordLength <= 7 -> 52.dp
        else -> 46.dp
    }
    val minTileSize = when {
        wordLength <= 7 -> 32.dp
        wordLength <= 9 -> 28.dp
        else -> 24.dp
    }
    val tileGap = when {
        wordLength >= 9 -> 3.dp
        wordLength >= 7 -> 4.dp
        else -> 6.dp
    }

    val maxKeyHeight = 52.dp
    val minKeyHeight = 40.dp
    val keyRowGap = 6.dp

    fun keyboardHeight(keyHeight: Dp): Dp = keyHeight * 3 + keyRowGap * 2 + 8.dp

    fun boardHeight(tileSize: Dp): Dp =
        tileSize * rowCount + tileGap * (rowCount - 1).coerceAtLeast(0)

    fun tileSizeForWidth(): Dp {
        val gaps = tileGap * (wordLength - 1)
        return ((contentWidth - gaps) / wordLength).coerceAtMost(maxTileSize)
    }

    fun totalHeight(tileSize: Dp, keyHeight: Dp): Dp =
        headerHeight +
            boardHeight(tileSize) +
            contentGaps +
            keyboardHeight(keyHeight)

    var tileSize = tileSizeForWidth().coerceAtMost(maxTileSize)
    var keyHeight = maxKeyHeight

    val availableHeight = maxHeight

    while (totalHeight(tileSize, keyHeight) > availableHeight) {
        when {
            keyHeight > minKeyHeight -> keyHeight -= 2.dp
            tileSize > minTileSize -> tileSize -= 1.dp
            else -> break
        }
    }

    val scrollContent = totalHeight(tileSize, keyHeight) > availableHeight
    val resolvedKeyHeight = keyHeight.coerceIn(minKeyHeight, maxKeyHeight)

    return GameLayoutSpec(
        tileSize = tileSize.coerceIn(minTileSize, maxTileSize),
        tileGap = tileGap,
        keyHeight = resolvedKeyHeight,
        keyboardHeight = keyboardHeight(resolvedKeyHeight),
        mastheadHeight = mastheadHeight,
        railHeight = railHeight,
        railGap = railGap,
        compactHeader = compactHeader,
        splitControls = splitControls,
        scrollContent = scrollContent,
    )
}
