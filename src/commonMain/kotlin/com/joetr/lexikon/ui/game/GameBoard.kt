package com.joetr.lexikon.ui.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joetr.lexikon.model.GameSnapshot
import com.joetr.lexikon.model.GameStatus
import com.joetr.lexikon.model.LetterMark
import com.joetr.lexikon.model.Tile
import com.joetr.lexikon.ui.theme.lexikonColors
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun GameBoard(
    snapshot: GameSnapshot,
    disableAnimations: Boolean,
    tileSize: Dp,
    tileGap: Dp = 6.dp,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .widthIn(max = tileSize * snapshot.wordLength + tileGap * (snapshot.wordLength - 1))
            .fillMaxWidth()
            .testTag("game-board"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(tileGap),
    ) {
        snapshot.rows.forEachIndexed { rowIndex, row ->
            val isCurrentRow = rowIndex == snapshot.rows.indexOfFirst { row ->
                row.tiles.all { it.mark == LetterMark.Empty || it.mark == LetterMark.Tbd }
            } && snapshot.status == com.joetr.lexikon.model.GameStatus.Playing
            GameRow(
                row = row,
                currentInput = if (isCurrentRow) snapshot.currentInput else "",
                wordLength = snapshot.wordLength,
                tileSize = tileSize,
                tileGap = tileGap,
                rowIndex = rowIndex,
                disableAnimations = disableAnimations,
            )
        }
    }
}

@Composable
private fun GameRow(
    row: com.joetr.lexikon.model.GuessRow,
    currentInput: String,
    wordLength: Int,
    tileSize: Dp,
    tileGap: Dp,
    rowIndex: Int,
    disableAnimations: Boolean,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(tileGap)) {
        for (i in 0 until wordLength) {
            val tile = row.tiles.getOrNull(i) ?: Tile(null, LetterMark.Empty)
            val displayChar = when {
                tile.char != null -> tile.char
                i < currentInput.length -> currentInput[i]
                else -> null
            }
            val mark = when {
                tile.mark != LetterMark.Empty && tile.mark != LetterMark.Tbd -> tile.mark
                displayChar != null -> LetterMark.Tbd
                else -> LetterMark.Empty
            }
            GameTile(
                char = displayChar,
                mark = mark,
                size = tileSize,
                revealDelayMs = if (disableAnimations) 0 else rowIndex * 80 + i * 120,
            )
        }
    }
}

@Composable
private fun GameTile(
    char: Char?,
    mark: LetterMark,
    size: Dp,
    revealDelayMs: Int,
) {
    val colors = lexikonColors()
    val rotation = remember { Animatable(0f) }
    var currentRevealedMark by remember {
        mutableStateOf<LetterMark?>(
            if (revealDelayMs == 0 && mark != LetterMark.Tbd && mark != LetterMark.Empty) mark else null,
        )
    }

    LaunchedEffect(mark, char) {
        if (mark != LetterMark.Tbd && mark != LetterMark.Empty && char != null) {
            rotation.snapTo(0f)
            currentRevealedMark = null
            if (revealDelayMs > 0) kotlinx.coroutines.delay(revealDelayMs.toLong())
            rotation.animateTo(90f, tween(300))
            currentRevealedMark = mark
            rotation.animateTo(180f, tween(300))
        } else {
            rotation.snapTo(0f)
            currentRevealedMark = null
        }
    }

    val showRevealedColor = currentRevealedMark != null

    val bg = if (showRevealedColor) {
        when (currentRevealedMark) {
            LetterMark.Correct -> colors.correct
            LetterMark.Present -> colors.present
            else -> colors.absent
        }
    } else {
        colors.tileEmpty
    }
    val fg = if (showRevealedColor) Color.White else colors.ink
    val borderColor = if (showRevealedColor) bg else colors.tileBorder
    Box(
        modifier = Modifier
            .size(size)
            .aspectRatio(1f)
            .graphicsLayer {
                val currentRot = if (mark == LetterMark.Tbd || mark == LetterMark.Empty) 0f else rotation.value
                rotationX = currentRot
                if (currentRot >= 90f) {
                    scaleY = -1f
                }
                cameraDistance = 12f * density
            }
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .border(1.5.dp, borderColor, RoundedCornerShape(4.dp))
            .testTag("tile-${char ?: '_'}"),
        contentAlignment = Alignment.Center,
    ) {
        if (char != null) {
            Text(
                text = char.toString(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.45).sp,
                    color = fg,
                    textAlign = TextAlign.Center,
                ),
            )
        }
    }
}
