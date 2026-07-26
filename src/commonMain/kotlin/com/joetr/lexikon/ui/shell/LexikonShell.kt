package com.joetr.lexikon.ui.shell

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import com.joetr.lexikon.ui.theme.LexikonIcons
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.joetr.lexikon.ui.game.letterFromKey
import com.joetr.lexikon.domain.GameController
import com.joetr.lexikon.domain.GameMessage
import com.joetr.lexikon.domain.PendingChange
import com.joetr.lexikon.model.Difficulty
import com.joetr.lexikon.model.GameMode
import com.joetr.lexikon.model.GameStatus
import com.joetr.lexikon.ui.dialogs.ConfirmSwitchDialog
import com.joetr.lexikon.ui.dialogs.HelpDialog
import com.joetr.lexikon.ui.dialogs.NextPuzzleCountdownDialog
import com.joetr.lexikon.ui.dialogs.PostGameBanner
import com.joetr.lexikon.ui.dialogs.SettingsDialog
import com.joetr.lexikon.ui.dialogs.StatsDialog
import com.joetr.lexikon.ui.game.GameBoard
import com.joetr.lexikon.ui.game.OnscreenKeyboard
import com.joetr.lexikon.ui.game.WinConfetti
import com.joetr.lexikon.ui.game.computeGameLayoutSpec
import com.joetr.lexikon.ui.theme.lexikonColors
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun LexikonShell(controller: GameController, disableAnimations: Boolean) {
    val colors = lexikonColors()
    val snackbar = remember { SnackbarHostState() }
    val shake = remember { Animatable(0f) }

    LaunchedEffect(controller.message) {
        when (val msg = controller.message) {
            is GameMessage.InvalidWord -> {
                showInputErrorFeedback(
                    snackbar = snackbar,
                    shake = shake,
                    text = null,
                    disableAnimations = disableAnimations,
                )
                controller.clearMessage()
            }
            is GameMessage.HardModeViolation -> {
                snackbar.showSnackbar("Must use all revealed hints (hard mode)")
                controller.clearMessage()
            }
            is GameMessage.NotEnoughLetters -> {
                showInputErrorFeedback(
                    snackbar = snackbar,
                    shake = shake,
                    text = "Not enough letters",
                    disableAnimations = disableAnimations,
                )
                controller.clearMessage()
            }
            is GameMessage.Copied -> {
                snackbar.showSnackbar("Copied to clipboard")
                controller.clearMessage()
            }
            GameMessage.None -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = colors.paper,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(colors.paper, colors.paper.copy(alpha = 0.92f), Color(0xFFE8E2D6)),
                    ),
                )
                .padding(padding)
                .onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                    when (event.key) {
                        Key.Backspace -> { controller.backspace(); true }
                        Key.Enter -> { controller.submit(); true }
                        else -> letterFromKey(event.key)?.let { controller.type(it); true } ?: false
                    }
                }
                .testTag("lexikon-shell"),
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxSize()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .graphicsLayer { translationX = shake.value },
            ) {
                val snapshot = controller.snapshot
                val layout = computeGameLayoutSpec(
                    maxWidth = maxWidth,
                    maxHeight = maxHeight,
                    wordLength = snapshot.wordLength,
                    rowCount = snapshot.rows.size,
                    status = snapshot.status,
                )
                val scrollState = rememberScrollState()

                Column(Modifier.fillMaxSize()) {
                    val contentModifier = if (layout.scrollContent) {
                        Modifier
                            .weight(1f)
                            .verticalScroll(scrollState)
                    } else {
                        Modifier.weight(1f)
                    }
                    Column(
                        modifier = contentModifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Header(
                            mode = snapshot.mode,
                            length = snapshot.wordLength,
                            difficulty = snapshot.difficulty,
                            compact = layout.compactHeader,
                            onModeChange = controller::requestModeChange,
                            onLengthChange = controller::requestLengthChange,
                            onDifficultyChange = controller::requestDifficultyChange,
                            onHelp = controller::openHelp,
                            onNextPuzzle = controller::openNextPuzzleCountdown,
                            onStats = controller::openStats,
                            onSettings = controller::openSettings,
                        )
                        if (layout.scrollContent) {
                            Spacer(Modifier.height(8.dp))
                            GameBoard(
                                snapshot = snapshot,
                                disableAnimations = disableAnimations,
                                tileSize = layout.tileSize,
                                tileGap = layout.tileGap,
                            )
                            PostGameBanner(
                                status = snapshot.status,
                                answer = snapshot.answer,
                                mode = snapshot.mode,
                                onCopy = controller::copyResult,
                                onNext = controller::startNextFreeGame,
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center,
                            ) {
                                GameBoard(
                                    snapshot = snapshot,
                                    disableAnimations = disableAnimations,
                                    tileSize = layout.tileSize,
                                    tileGap = layout.tileGap,
                                )
                            }
                            PostGameBanner(
                                status = snapshot.status,
                                answer = snapshot.answer,
                                mode = snapshot.mode,
                                onCopy = controller::copyResult,
                                onNext = controller::startNextFreeGame,
                            )
                        }
                    }
                    OnscreenKeyboard(
                        keyboardMarks = controller.keyboardMarks,
                        onLetter = controller::type,
                        onBackspace = controller::backspace,
                        onSubmit = controller::submit,
                        enabled = snapshot.status == GameStatus.Playing,
                        keyHeight = layout.keyHeight,
                    )
                }
            }

            WinConfetti(
                show = controller.snapshot.status == GameStatus.Won && !disableAnimations,
            )
        }
    }

    if (controller.showHelp) HelpDialog(onDismiss = controller::dismissHelp)
    if (controller.showNextPuzzleCountdown) {
        NextPuzzleCountdownDialog(
            timeUntilNextPuzzle = controller::timeUntilNextDailyPuzzle,
            onDismiss = controller::closeNextPuzzleCountdown,
        )
    }
    if (controller.showStats) {
        StatsDialog(
            length = controller.snapshot.wordLength,
            difficulty = controller.snapshot.difficulty,
            stats = controller.currentLengthStats(),
            maxGuesses = controller.snapshot.maxGuesses,
            onCopy = controller::copyResult,
            onDismiss = controller::closeStats,
        )
    }
    if (controller.showSettings) {
        SettingsDialog(
            hardMode = controller.settings.hardMode,
            colorblind = controller.settings.colorblind,
            onHardModeChange = controller::updateHardMode,
            onColorblindChange = controller::updateColorblind,
            onDismiss = controller::closeSettings,
        )
    }
    controller.pendingConfirm?.let { pending ->
        ConfirmSwitchDialog(
            mode = pending.mode,
            length = pending.length,
            difficulty = pending.difficulty,
            onConfirm = controller::confirmPendingChange,
            onDismiss = controller::cancelPendingChange,
        )
    }
}

@Composable
private fun Header(
    mode: GameMode,
    length: Int,
    difficulty: Difficulty,
    compact: Boolean,
    onModeChange: (GameMode) -> Unit,
    onLengthChange: (Int) -> Unit,
    onDifficultyChange: (Difficulty) -> Unit,
    onHelp: () -> Unit,
    onNextPuzzle: () -> Unit,
    onStats: () -> Unit,
    onSettings: () -> Unit,
) {
    val colors = lexikonColors()
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "Lexikon",
            style = if (compact) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.displayLarge,
            color = colors.ink,
            modifier = Modifier.testTag("brand-title"),
        )
        Spacer(Modifier.height(if (compact) 8.dp else 12.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FilterChip(
                    selected = mode == GameMode.Daily,
                    onClick = { onModeChange(GameMode.Daily) },
                    label = { Text("Daily") },
                    modifier = Modifier.testTag("mode-daily"),
                )
                FilterChip(
                    selected = mode == GameMode.Free,
                    onClick = { onModeChange(GameMode.Free) },
                    label = { Text("Free") },
                    modifier = Modifier.testTag("mode-free"),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(onClick = onHelp, modifier = Modifier.testTag("help-button")) {
                    Text("?", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = colors.ink)
                }
                IconButton(onClick = onNextPuzzle, modifier = Modifier.testTag("next-puzzle-button")) {
                    Icon(
                        imageVector = LexikonIcons.Clock,
                        contentDescription = "Next word countdown",
                        tint = colors.ink,
                        modifier = Modifier.size(20.dp),
                    )
                }
                IconButton(onClick = onStats, modifier = Modifier.testTag("stats-button")) {
                    Icon(
                        imageVector = LexikonIcons.Stats,
                        contentDescription = "Stats",
                        tint = colors.ink,
                        modifier = Modifier.size(20.dp),
                    )
                }
                IconButton(onClick = onSettings, modifier = Modifier.testTag("settings-button")) {
                    Icon(
                        imageVector = LexikonIcons.Settings,
                        contentDescription = "Settings",
                        tint = colors.ink,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(if (compact) 6.dp else 8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("length-selector"),
        ) {
            for (len in 5..10) {
                FilterChip(
                    selected = length == len,
                    onClick = { onLengthChange(len) },
                    label = { Text(len.toString()) },
                    modifier = Modifier.testTag("length-$len"),
                )
            }
        }
        Spacer(Modifier.height(if (compact) 4.dp else 6.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("difficulty-selector"),
        ) {
            for (level in Difficulty.entries) {
                FilterChip(
                    selected = difficulty == level,
                    onClick = { onDifficultyChange(level) },
                    label = { Text(level.name) },
                    modifier = Modifier.testTag("difficulty-${level.slug}"),
                )
            }
        }
    }
}

@Composable
private fun TextButtonIcon(label: String, onClick: () -> Unit, tag: String) {
    IconButton(onClick = onClick, modifier = Modifier.testTag(tag)) {
        Text(label, style = MaterialTheme.typography.titleMedium)
    }
}

private suspend fun showInputErrorFeedback(
    snackbar: SnackbarHostState,
    shake: Animatable<Float, *>,
    text: String?,
    disableAnimations: Boolean,
) {
    coroutineScope {
        launch {
            if (!disableAnimations) {
                shake.snapTo(0f)
                repeat(3) {
                    shake.animateTo(8f, tween(50))
                    shake.animateTo(-8f, tween(50))
                }
                shake.animateTo(0f, tween(50))
            }
        }
        if (text != null) {
            snackbar.showSnackbar(text)
        }
    }
}
