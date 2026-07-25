package com.joetr.lexikon.ui.game

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.vinceglb.confettikit.compose.ConfettiKit
import io.github.vinceglb.confettikit.core.Party
import io.github.vinceglb.confettikit.core.Position
import io.github.vinceglb.confettikit.core.emitter.Emitter
import kotlin.time.Duration.Companion.milliseconds

private val CONFETTI_COLORS = listOf(0xfce18a, 0xff726d, 0xf4306d, 0x24234f)

@Composable
fun WinConfetti(show: Boolean) {
    if (!show) return
    ConfettiKit(
        modifier = Modifier.fillMaxSize(),
        parties = listOf(
            Party(
                angle = 270,
                spread = 360,
                speed = 10f,
                maxSpeed = 30f,
                colors = CONFETTI_COLORS,
                position = Position.Relative(0.5, 0.0),
                emitter = Emitter(duration = 150.milliseconds).max(200),
            ),
        ),
    )
}
