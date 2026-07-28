package com.joetr.lexikon.ui.shell

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import com.joetr.lexikon.ui.theme.LexikonIcons
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joetr.lexikon.ui.game.letterFromKey
import com.joetr.lexikon.domain.GameController
import com.joetr.lexikon.domain.GameMessage
import com.joetr.lexikon.model.Difficulty
import com.joetr.lexikon.model.GameMode
import com.joetr.lexikon.model.GameStatus
import com.joetr.lexikon.ui.dialogs.ConfirmSwitchDialog
import com.joetr.lexikon.ui.dialogs.HelpDialog
import com.joetr.lexikon.ui.dialogs.NextPuzzleCountdownDialog
import com.joetr.lexikon.ui.dialogs.SettingsDialog
import com.joetr.lexikon.ui.dialogs.StatsDialog
import com.joetr.lexikon.ui.game.GameBoard
import com.joetr.lexikon.ui.game.OnscreenKeyboard
import com.joetr.lexikon.ui.game.PostGamePanel
import com.joetr.lexikon.ui.game.WinConfetti
import com.joetr.lexikon.ui.game.computeGameLayoutSpec
import com.joetr.lexikon.ui.theme.lexikonColors
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/** Frames to keep asking for focus before giving up, so a slow first layout still lands. */
private const val FOCUS_REQUEST_ATTEMPTS = 10

@Composable
fun LexikonShell(controller: GameController, disableAnimations: Boolean) {
    val colors = lexikonColors()
    val snackbar = remember { SnackbarHostState() }
    val shake = remember { Animatable(0f) }
    val boardFocus = remember { FocusRequester() }

    // Key events only reach the shell while something inside it holds focus, so the board
    // claims focus up front and takes it back whenever a dialog or the on-screen keyboard
    // hands it off. Without this the physical keyboard stays dead until the first tap.
    val dialogOpen = controller.showHelp || controller.showStats || controller.showSettings ||
        controller.showNextPuzzleCountdown || controller.pendingConfirm != null
    LaunchedEffect(dialogOpen, controller.snapshot.status) {
        if (dialogOpen) return@LaunchedEffect
        // The box is not placed yet on the first pass, and a request made before that is
        // rejected, so keep asking for a few frames until it sticks.
        repeat(FOCUS_REQUEST_ATTEMPTS) {
            if (runCatching { boardFocus.requestFocus() }.getOrDefault(false)) {
                return@LaunchedEffect
            }
            withFrameNanos { }
        }
    }

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
                .focusRequester(boardFocus)
                .onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                    when (event.key) {
                        Key.Backspace -> { controller.backspace(); true }
                        Key.Enter -> { controller.submit(); true }
                        else -> letterFromKey(event.key)?.let { controller.type(it); true } ?: false
                    }
                }
                .focusTarget()
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
                            mastheadHeight = layout.mastheadHeight,
                            railHeight = layout.railHeight,
                            railGap = layout.railGap,
                            splitControls = layout.splitControls,
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
                            Spacer(Modifier.height(12.dp))
                            GameBoard(
                                snapshot = snapshot,
                                disableAnimations = disableAnimations,
                                tileSize = layout.tileSize,
                                tileGap = layout.tileGap,
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
                        }
                    }
                    // The result surface takes over the keyboard's footprint instead of
                    // adding a row, so the board never moves when a game ends.
                    if (snapshot.status == GameStatus.Playing) {
                        OnscreenKeyboard(
                            keyboardMarks = controller.keyboardMarks,
                            onLetter = controller::type,
                            onBackspace = controller::backspace,
                            onSubmit = controller::submit,
                            enabled = true,
                            keyHeight = layout.keyHeight,
                        )
                    } else {
                        PostGamePanel(
                            status = snapshot.status,
                            answer = snapshot.answer,
                            mode = snapshot.mode,
                            height = layout.keyboardHeight,
                            disableAnimations = disableAnimations,
                            onCopy = controller::copyResult,
                            onNext = controller::startNextFreeGame,
                        )
                    }
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

/**
 * Two rows: a wordmark row carrying the icon actions, and a control rail carrying mode,
 * word length and difficulty. On narrow viewports the rail splits into two.
 */
@Composable
private fun Header(
    mode: GameMode,
    length: Int,
    difficulty: Difficulty,
    mastheadHeight: Dp,
    railHeight: Dp,
    railGap: Dp,
    splitControls: Boolean,
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
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().height(mastheadHeight),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Lexikon",
                style = if (compact) {
                    MaterialTheme.typography.headlineSmall
                } else {
                    MaterialTheme.typography.headlineMedium
                },
                color = colors.ink,
                modifier = Modifier.testTag("brand-title"),
            )
            val buttonSize = if (compact) 34.dp else 38.dp
            val iconSize = if (compact) 17.dp else 19.dp
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onHelp,
                    modifier = Modifier.size(buttonSize).testTag("help-button"),
                ) {
                    Text(
                        "?",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                        ),
                        color = colors.inkMuted,
                    )
                }
                HeaderIconButton(LexikonIcons.Clock, "Next word countdown", onNextPuzzle, "next-puzzle-button", buttonSize, iconSize)
                HeaderIconButton(LexikonIcons.Stats, "Stats", onStats, "stats-button", buttonSize, iconSize)
                HeaderIconButton(LexikonIcons.Settings, "Settings", onSettings, "settings-button", buttonSize, iconSize)
            }
        }
        Spacer(Modifier.height(railGap))
        if (splitControls) {
            ControlRail(railHeight) {
                ModeGroup(mode, onModeChange, compact)
                RailDivider(railHeight)
                DifficultyGroup(difficulty, onDifficultyChange, compact)
            }
            Spacer(Modifier.height(6.dp))
            ControlRail(railHeight) {
                LengthGroup(length, onLengthChange, compact)
            }
        } else {
            ControlRail(railHeight) {
                ModeGroup(mode, onModeChange, compact)
                RailDivider(railHeight)
                LengthGroup(length, onLengthChange, compact)
                RailDivider(railHeight)
                DifficultyGroup(difficulty, onDifficultyChange, compact)
            }
        }
    }
}

@Composable
private fun HeaderIconButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    tag: String,
    buttonSize: Dp,
    iconSize: Dp,
) {
    val colors = lexikonColors()
    IconButton(onClick = onClick, modifier = Modifier.size(buttonSize).testTag(tag)) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = colors.inkMuted,
            modifier = Modifier.size(iconSize),
        )
    }
}

/** A single hairline-bordered bar that holds one or more segment groups. */
@Composable
private fun ControlRail(
    height: Dp,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = lexikonColors()
    val shape = RoundedCornerShape(7.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(colors.tileEmpty.copy(alpha = 0.65f))
            .border(1.dp, colors.tileBorder, shape)
            .padding(3.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
private fun RowScope.ModeGroup(mode: GameMode, onModeChange: (GameMode) -> Unit, compact: Boolean) {
    Row(
        modifier = Modifier.weight(groupWeight("Daily", "Free")).fillMaxHeight().testTag("mode-selector"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RailSegment("Daily", mode == GameMode.Daily, { onModeChange(GameMode.Daily) }, "mode-daily", compact)
        RailSegment("Free", mode == GameMode.Free, { onModeChange(GameMode.Free) }, "mode-free", compact)
    }
}

@Composable
private fun RowScope.LengthGroup(length: Int, onLengthChange: (Int) -> Unit, compact: Boolean) {
    val labels = (5..10).map { it.toString() }
    Row(
        modifier = Modifier.weight(groupWeight(*labels.toTypedArray())).fillMaxHeight().testTag("length-selector"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (len in 5..10) {
            RailSegment(len.toString(), length == len, { onLengthChange(len) }, "length-$len", compact)
        }
    }
}

@Composable
private fun RowScope.DifficultyGroup(
    difficulty: Difficulty,
    onDifficultyChange: (Difficulty) -> Unit,
    compact: Boolean,
) {
    val labels = Difficulty.entries.map { it.name }
    Row(
        modifier = Modifier.weight(groupWeight(*labels.toTypedArray())).fillMaxHeight().testTag("difficulty-selector"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (level in Difficulty.entries) {
            RailSegment(
                level.name,
                difficulty == level,
                { onDifficultyChange(level) },
                "difficulty-${level.slug}",
                compact,
            )
        }
    }
}

@Composable
private fun RowScope.RailSegment(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    tag: String,
    compact: Boolean,
) {
    val colors = lexikonColors()
    val background by animateColorAsState(
        targetValue = if (selected) colors.ink else Color.Transparent,
        animationSpec = tween(durationMillis = 160),
        label = "railSegmentBackground",
    )
    val foreground by animateColorAsState(
        targetValue = if (selected) colors.paper else colors.inkMuted,
        animationSpec = tween(durationMillis = 160),
        label = "railSegmentForeground",
    )
    Box(
        modifier = Modifier
            .weight(labelWeight(label))
            .fillMaxHeight()
            .clip(RoundedCornerShape(4.dp))
            .background(background)
            .clickable(role = Role.Button, onClick = onClick)
            .testTag(tag),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = if (compact) 12.sp else 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                letterSpacing = 0.2.sp,
            ),
            color = foreground,
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Composable
private fun RailDivider(railHeight: Dp) {
    Box(
        modifier = Modifier
            .padding(horizontal = 5.dp)
            .width(1.dp)
            .height((railHeight.value - 16f).coerceAtLeast(8f).dp)
            .background(lexikonColors().tileBorder),
    )
}

/**
 * Segments size themselves by label length so "Medium" is never squeezed next to "5".
 * A group's weight is the sum of its members', which keeps the proportions identical
 * whether the group sits in a shared rail or one of its own.
 */
private fun labelWeight(label: String): Float = when (label.length) {
    1 -> 1f
    2 -> 1.25f
    3, 4 -> 1.6f
    5 -> 1.8f
    else -> 2f
}

private fun groupWeight(vararg labels: String): Float = labels.sumOf { labelWeight(it).toDouble() }.toFloat()

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
