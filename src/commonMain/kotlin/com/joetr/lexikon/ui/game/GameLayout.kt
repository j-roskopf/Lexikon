package com.joetr.lexikon.ui.game

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.joetr.lexikon.model.GameStatus

data class GameLayoutSpec(
    val tileSize: Dp,
    val tileGap: Dp,
    val keyHeight: Dp,
    val compactHeader: Boolean,
    val scrollContent: Boolean,
)

internal fun computeGameLayoutSpec(
    maxWidth: Dp,
    maxHeight: Dp,
    wordLength: Int,
    rowCount: Int,
    status: GameStatus,
    horizontalPadding: Dp = 16.dp,
    verticalPadding: Dp = 12.dp,
): GameLayoutSpec {
    val contentWidth = maxWidth - horizontalPadding * 2
    val hasPostGameBanner = status != GameStatus.Playing

    val compactHeader = maxHeight < 640.dp || maxWidth < 360.dp
    // Title, mode row, length chips and difficulty chips. Keep in sync with Header.
    val headerHeight = if (compactHeader) 160.dp else 202.dp
    val bannerHeight = if (hasPostGameBanner) 68.dp else 0.dp
    val contentGaps = 12.dp + if (hasPostGameBanner) 8.dp else 0.dp

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
        verticalPadding * 2 +
            headerHeight +
            boardHeight(tileSize) +
            bannerHeight +
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

    return GameLayoutSpec(
        tileSize = tileSize.coerceIn(minTileSize, maxTileSize),
        tileGap = tileGap,
        keyHeight = keyHeight.coerceIn(minKeyHeight, maxKeyHeight),
        compactHeader = compactHeader,
        scrollContent = scrollContent,
    )
}
