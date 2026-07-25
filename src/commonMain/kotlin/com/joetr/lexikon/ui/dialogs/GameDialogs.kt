package com.joetr.lexikon.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import com.joetr.lexikon.model.Difficulty
import com.joetr.lexikon.model.GameMode
import com.joetr.lexikon.model.GameStatus
import androidx.compose.foundation.shape.RoundedCornerShape
import com.joetr.lexikon.model.LengthStats
import kotlin.time.Duration

@Composable
fun HelpDialog(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f))
            .testTag("help-dialog"),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 6.dp,
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 360.dp),
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("How to play", style = MaterialTheme.typography.titleMedium)
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Guess the word in the allowed number of tries.")
                    Text("• Each guess must be a valid word.")
                    Text("• Green = correct letter, correct spot.")
                    Text("• Yellow = correct letter, wrong spot.")
                    Text("• Gray = letter not in the word.")
                    Text("A new daily puzzle is available each day for each word length (5–10).")
                }
                Button(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Got it")
                }
            }
        }
    }
}

@Composable
fun NextPuzzleCountdownDialog(
    timeUntilNextPuzzle: () -> Duration,
    onDismiss: () -> Unit,
) {
    var remaining by remember { mutableStateOf(timeUntilNextPuzzle()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            remaining = timeUntilNextPuzzle()
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Next word") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("A new daily puzzle unlocks at midnight, US Eastern time.")
                Text(
                    formatCountdown(remaining),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.testTag("next-puzzle-countdown-value"),
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Close") }
        },
        modifier = Modifier.testTag("next-puzzle-countdown-dialog"),
    )
}

private fun formatCountdown(duration: Duration): String {
    val totalSeconds = duration.inWholeSeconds.coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}

@Composable
fun StatsDialog(
    length: Int,
    difficulty: Difficulty,
    stats: LengthStats,
    maxGuesses: Int,
    onCopy: () -> Unit,
    onDismiss: () -> Unit,
) {
    val winPct = if (stats.played > 0) (stats.wins * 100) / stats.played else 0
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Statistics ($length letters, ${difficulty.name})") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatBlock("Played", stats.played.toString())
                    StatBlock("Win %", "$winPct")
                    StatBlock("Streak", stats.currentStreak.toString())
                    StatBlock("Max", stats.maxStreak.toString())
                }
                HorizontalDivider()
                Text("Guess distribution", style = MaterialTheme.typography.titleMedium)
                val maxBar = (stats.guessDistribution.maxOrNull() ?: 1).coerceAtLeast(1)
                for (i in 0 until maxGuesses) {
                    val count = stats.guessDistribution.getOrElse(i) { 0 }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("${i + 1}", modifier = Modifier.padding(end = 8.dp))
                        val fraction = count.toFloat() / maxBar
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(20.dp)
                                .padding(end = 8.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction.coerceAtLeast(if (count > 0) 0.08f else 0f))
                                    .height(20.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                        RoundedCornerShape(2.dp),
                                    ),
                            )
                        }
                        Text(count.toString())
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onCopy) { Text("Copy") }
                Button(onClick = onDismiss) { Text("Close") }
            }
        },
        modifier = Modifier.testTag("stats-dialog"),
    )
}

@Composable
private fun StatBlock(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium)
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun SettingsDialog(
    hardMode: Boolean,
    colorblind: Boolean,
    onHardModeChange: (Boolean) -> Unit,
    onColorblindChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settings") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Hard mode")
                    Switch(checked = hardMode, onCheckedChange = onHardModeChange)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Colorblind palette")
                    Switch(checked = colorblind, onCheckedChange = onColorblindChange)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Done") }
        },
        modifier = Modifier.testTag("settings-dialog"),
    )
}

@Composable
fun ConfirmSwitchDialog(
    mode: GameMode,
    length: Int,
    difficulty: Difficulty,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Start a new game?") },
        text = {
            Text(
                "Changing to ${mode.name.lowercase()} mode " +
                    "($length letters, ${difficulty.name.lowercase()}) " +
                    "will abandon your current progress.",
            )
        },
        confirmButton = {
            Button(onClick = onConfirm, modifier = Modifier.testTag("confirm-switch")) { Text("Continue") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        modifier = Modifier.testTag("confirm-dialog"),
    )
}

@Composable
fun PostGameBanner(
    status: GameStatus,
    answer: String,
    mode: GameMode,
    onCopy: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (status == GameStatus.Playing) return
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp).testTag("post-game-banner"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            when (status) {
                GameStatus.Won -> "Splendid!"
                GameStatus.Lost -> "The word was $answer"
                GameStatus.Playing -> ""
            },
            style = MaterialTheme.typography.titleMedium,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onCopy, modifier = Modifier.testTag("copy-result")) {
                Text("Copy result")
            }
            if (mode == GameMode.Free) {
                Button(onClick = onNext, modifier = Modifier.testTag("next-game")) {
                    Text("Next puzzle")
                }
            }
        }
    }
}
