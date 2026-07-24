package com.joetr.lexikon.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.joetr.lexikon.model.LetterMark
import com.joetr.lexikon.ui.theme.LexikonIcons
import com.joetr.lexikon.ui.theme.lexikonColors

private val ROW1 = "QWERTYUIOP"
private val ROW2 = "ASDFGHJKL"
private val ROW3 = "ZXCVBNM"

@Composable
fun OnscreenKeyboard(
    keyboardMarks: Map<Char, LetterMark>,
    onLetter: (Char) -> Unit,
    onBackspace: () -> Unit,
    onSubmit: () -> Unit,
    enabled: Boolean,
    keyHeight: androidx.compose.ui.unit.Dp = 52.dp,
    modifier: Modifier = Modifier,
) {
    val colors = lexikonColors()
    val iconSize = (keyHeight.value * 0.38f).dp
    val rowInset = (keyHeight.value * 0.23f).dp
    Column(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 520.dp)
            .padding(horizontal = 4.dp)
            .testTag("onscreen-keyboard"),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        KeyboardRow(ROW1, keyboardMarks, onLetter, enabled, keyHeight)
        KeyboardRow(ROW2, keyboardMarks, onLetter, enabled, keyHeight, horizontalPadding = rowInset)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        ) {
            Button(
                onClick = onSubmit,
                enabled = enabled,
                modifier = Modifier.height(keyHeight).defaultMinSize(minWidth = 36.dp).weight(1.3f).testTag("key-enter"),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accent,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Icon(
                    imageVector = LexikonIcons.Enter,
                    contentDescription = "Enter",
                    tint = Color.White,
                    modifier = Modifier.size(iconSize),
                )
            }
            ROW3.forEach { letter ->
                KeyButton(letter, keyboardMarks[letter], onLetter, enabled, keyHeight, Modifier.weight(1f))
            }
            Button(
                onClick = onBackspace,
                enabled = enabled,
                modifier = Modifier.height(keyHeight).defaultMinSize(minWidth = 36.dp).weight(1.3f).testTag("key-backspace"),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.absent.copy(alpha = 0.25f),
                    contentColor = colors.ink,
                ),
            ) {
                Icon(
                    imageVector = LexikonIcons.Backspace,
                    contentDescription = "Backspace",
                    tint = colors.ink,
                    modifier = Modifier.size(iconSize),
                )
            }
        }
    }
}

@Composable
private fun KeyboardRow(
    letters: String,
    marks: Map<Char, LetterMark>,
    onLetter: (Char) -> Unit,
    enabled: Boolean,
    keyHeight: androidx.compose.ui.unit.Dp,
    horizontalPadding: androidx.compose.ui.unit.Dp = 0.dp,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        letters.forEach { letter ->
            KeyButton(letter, marks[letter], onLetter, enabled, keyHeight, Modifier.weight(1f))
        }
    }
}

@Composable
private fun KeyButton(
    letter: Char,
    mark: LetterMark?,
    onLetter: (Char) -> Unit,
    enabled: Boolean,
    keyHeight: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    val colors = lexikonColors()
    val bg = when (mark) {
        LetterMark.Correct -> colors.correct
        LetterMark.Present -> colors.present
        LetterMark.Absent -> colors.absent
        else -> colors.tileEmpty
    }
    val fg = when (mark) {
        LetterMark.Correct, LetterMark.Present, LetterMark.Absent -> Color.White
        else -> colors.ink
    }
    Button(
        onClick = { onLetter(letter) },
        enabled = enabled,
        modifier = modifier.height(keyHeight).defaultMinSize(minWidth = 28.dp).testTag("key-$letter"),
        shape = RoundedCornerShape(4.dp),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = bg,
            contentColor = fg,
            disabledContainerColor = bg.copy(alpha = 0.5f),
            disabledContentColor = fg.copy(alpha = 0.5f),
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
    ) {
        Text(
            letter.toString(),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        )
    }
}
