package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

data class BentoColors(
    val isDark: Boolean = false,
    val background: Color,
    val surface: Color,
    val surfaceHeader: Color,
    val surfaceVariant: Color,
    val cardBorder: Color,
    val cardBorderSubtle: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val tilePurple: Color,
    val tilePurpleText: Color,
    val tileAmber: Color,
    val tileAmberText: Color,
    val tileGreen: Color,
    val tileGreenText: Color,
    val tileBlue: Color,
    val tileBlueText: Color,
    val tilePink: Color,
    val tilePinkText: Color,
    val inputContainer: Color,
    val inputBorder: Color,
    val subtleSurface: Color,
    val primaryAccent: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color
)

private val LightBentoColors = BentoColors(
    isDark = false,
    background = BentoBackground,
    surface = Color.White,
    surfaceHeader = Color.White,
    surfaceVariant = Color(0xFFF1F5F9),
    cardBorder = Slate200,
    cardBorderSubtle = Color(0xFFF1F5F9),
    textPrimary = Slate900,
    textSecondary = Slate600,
    textMuted = Slate400,
    tilePurple = BentoTilePurple,
    tilePurpleText = BentoPrimary,
    tileAmber = BentoTileAmber,
    tileAmberText = Color(0xFFB45309),
    tileGreen = BentoTileGreen,
    tileGreenText = SafetyGreen,
    tileBlue = BentoTileBlue,
    tileBlueText = SafetyBlue,
    tilePink = BentoTilePink,
    tilePinkText = SafetyRed,
    inputContainer = Color.White,
    inputBorder = Slate300,
    subtleSurface = BentoBackground,
    primaryAccent = BentoPrimary,
    primaryContainer = BentoPrimaryContainer,
    onPrimaryContainer = BentoPrimaryDark
)

val LocalBentoColors = staticCompositionLocalOf { LightBentoColors }

val MaterialTheme.bento: BentoColors
    @Composable
    get() = LocalBentoColors.current

private val LightColorScheme = lightColorScheme(
    primary = BentoPrimary,
    onPrimary = Color.White,
    primaryContainer = BentoPrimaryContainer,
    onPrimaryContainer = BentoPrimaryDark,
    secondary = Slate700,
    onSecondary = Color.White,
    secondaryContainer = BentoTilePurple,
    onSecondaryContainer = BentoPrimaryDark,
    tertiary = BentoLavenderCard,
    background = BentoBackground,
    surface = Color.White,
    onBackground = Slate900,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600,
    outline = Slate200
)

@Composable
fun appTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MaterialTheme.bento.textPrimary,
    unfocusedTextColor = MaterialTheme.bento.textPrimary,
    disabledTextColor = MaterialTheme.bento.textMuted,
    focusedContainerColor = MaterialTheme.bento.inputContainer,
    unfocusedContainerColor = MaterialTheme.bento.inputContainer,
    focusedBorderColor = MaterialTheme.bento.primaryAccent,
    unfocusedBorderColor = MaterialTheme.bento.inputBorder,
    focusedLabelColor = MaterialTheme.bento.primaryAccent,
    unfocusedLabelColor = MaterialTheme.bento.textSecondary,
    focusedPlaceholderColor = MaterialTheme.bento.textMuted,
    unfocusedPlaceholderColor = MaterialTheme.bento.textMuted,
    cursorColor = MaterialTheme.bento.primaryAccent
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val bentoColors = LightBentoColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = bentoColors.surfaceHeader.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            }
        }
    }

    CompositionLocalProvider(LocalBentoColors provides bentoColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

