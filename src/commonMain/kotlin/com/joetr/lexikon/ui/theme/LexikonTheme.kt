package com.joetr.lexikon.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

import com.joetr.lexikon.lexikon.generated.resources.Res
import com.joetr.lexikon.lexikon.generated.resources.inter
import com.joetr.lexikon.lexikon.generated.resources.lora
import org.jetbrains.compose.resources.Font

data class LexikonColors(
    val paper: Color,
    val ink: Color,
    val inkMuted: Color,
    val correct: Color,
    val present: Color,
    val absent: Color,
    val tileEmpty: Color,
    val tileBorder: Color,
    val accent: Color,
)

val LocalLexikonColors = staticCompositionLocalOf {
    LexikonColors(
        paper = Color(0xFFF4F0E8),
        ink = Color(0xFF1A1A18),
        inkMuted = Color(0xFF5C5A54),
        correct = Color(0xFF3D7A4A),
        present = Color(0xFFB8860B),
        absent = Color(0xFF6B6860),
        tileEmpty = Color(0xFFFDFCF9),
        tileBorder = Color(0xFFC8C2B6),
        accent = Color(0xFF2C4A6E),
    )
}

val ColorblindLexikonColors = LexikonColors(
    paper = Color(0xFFF4F0E8),
    ink = Color(0xFF1A1A18),
    inkMuted = Color(0xFF5C5A54),
    correct = Color(0xFF0077BB),
    present = Color(0xFFEE7733),
    absent = Color(0xFF6B6860),
    tileEmpty = Color(0xFFFDFCF9),
    tileBorder = Color(0xFFC8C2B6),
    accent = Color(0xFF2C4A6E),
)

@Composable
fun LexikonTheme(colorblind: Boolean, content: @Composable () -> Unit) {
    val displayFamily = FontFamily(Font(Res.font.lora, FontWeight.Bold))
    val bodyFamily = FontFamily(
        Font(Res.font.inter, FontWeight.Normal),
        Font(Res.font.inter, FontWeight.Bold),
    )
    val lexColors = if (colorblind) ColorblindLexikonColors else LocalLexikonColors.current
    val scheme = lightColorScheme(
        primary = lexColors.accent,
        onPrimary = Color.White,
        background = lexColors.paper,
        onBackground = lexColors.ink,
        surface = lexColors.tileEmpty,
        onSurface = lexColors.ink,
        surfaceVariant = Color(0xFFE8E3D8),
        onSurfaceVariant = lexColors.inkMuted,
    )
    val typography = Typography(
        displayLarge = TextStyle(fontFamily = displayFamily, fontWeight = FontWeight.Bold, fontSize = 42.sp, letterSpacing = (-0.5).sp),
        headlineMedium = TextStyle(fontFamily = displayFamily, fontWeight = FontWeight.Bold, fontSize = 28.sp),
        titleMedium = TextStyle(fontFamily = bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
        bodyMedium = TextStyle(fontFamily = bodyFamily, fontWeight = FontWeight.Normal, fontSize = 15.sp),
        labelLarge = TextStyle(fontFamily = bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, letterSpacing = 0.5.sp),
    )
    CompositionLocalProvider(LocalLexikonColors provides lexColors) {
        MaterialTheme(colorScheme = scheme, typography = typography, content = content)
    }
}

@Composable
fun lexikonColors(): LexikonColors = LocalLexikonColors.current
