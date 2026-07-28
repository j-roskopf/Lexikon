package com.joetr.lexikon.ui.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joetr.lexikon.model.GameMode
import com.joetr.lexikon.model.GameStatus
import com.joetr.lexikon.ui.theme.lexikonColors

/**
 * Result surface shown once the puzzle is over. It takes over the on-screen keyboard's
 * footprint (the keyboard is dead weight at that point) so finishing a game never adds a
 * row or resizes the board.
 */
@Composable
fun PostGamePanel(
    status: GameStatus,
    answer: String,
    mode: GameMode,
    height: Dp,
    disableAnimations: Boolean,
    onCopy: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (status == GameStatus.Playing) return
    val colors = lexikonColors()
    val enter = remember { Animatable(if (disableAnimations) 1f else 0f) }
    LaunchedEffect(status) {
        if (!disableAnimations) {
            enter.snapTo(0f)
            enter.animateTo(1f, tween(durationMillis = 320, easing = LinearOutSlowInEasing))
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .graphicsLayer {
                alpha = enter.value
                translationY = (1f - enter.value) * 12.dp.toPx()
            }
            .testTag("post-game-banner"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (status == GameStatus.Won) {
            Text(
                "Splendid!",
                style = MaterialTheme.typography.headlineMedium,
                color = colors.ink,
            )
        } else {
            Text(
                "The word was",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.inkMuted,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                answer.uppercase(),
                style = MaterialTheme.typography.headlineMedium.copy(letterSpacing = 2.sp),
                color = colors.ink,
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (mode == GameMode.Free) {
                PanelButton("Next puzzle", primary = true, onClick = onNext, tag = "next-game")
                PanelButton("Copy result", primary = false, onClick = onCopy, tag = "copy-result")
            } else {
                PanelButton("Copy result", primary = true, onClick = onCopy, tag = "copy-result")
            }
        }
    }
}

@Composable
private fun PanelButton(
    label: String,
    primary: Boolean,
    onClick: () -> Unit,
    tag: String,
) {
    val colors = lexikonColors()
    val shape = RoundedCornerShape(6.dp)
    val contentPadding = PaddingValues(horizontal = 20.dp)
    if (primary) {
        Button(
            onClick = onClick,
            modifier = Modifier.height(44.dp).testTag(tag),
            shape = shape,
            contentPadding = contentPadding,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.accent,
                contentColor = Color.White,
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier.height(44.dp).testTag(tag),
            shape = shape,
            contentPadding = contentPadding,
            border = BorderStroke(1.dp, colors.tileBorder),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.ink),
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}
