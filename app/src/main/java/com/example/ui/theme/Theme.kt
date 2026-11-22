package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.model.AccentPreset
import com.example.model.ThemeMode

val LocalXPhotosPalette = staticCompositionLocalOf { DarkXPhotosPalette }
val LocalAnimScale = staticCompositionLocalOf { 1.0f }

private val XPhotosShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(22.dp)
)

@Composable
fun XPhotosTheme(
    themeMode: ThemeMode,
    accentPreset: AccentPreset = AccentPreset.Violet,
    pureAmoledDark: Boolean = false,
    animationSpeedScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val useDark = when (themeMode) {
        ThemeMode.system -> systemDark
        ThemeMode.light -> false
        ThemeMode.dark -> true
    }

    val palette = buildPalette(
        useDark = useDark,
        accentPreset = accentPreset,
        pureAmoledDark = pureAmoledDark
    )
    val colorScheme = if (useDark) {
        darkColorScheme(
            primary = palette.primary,
            onPrimary = Color.White,
            primaryContainer = palette.primarySoft,
            secondary = palette.accent,
            tertiary = palette.gold,
            background = palette.bg,
            onBackground = palette.text,
            surface = palette.surface,
            onSurface = palette.text,
            surfaceVariant = palette.surfaceAlt,
            onSurfaceVariant = palette.textSecondary,
            outline = palette.border,
            outlineVariant = palette.borderStrong,
            error = palette.danger
        )
    } else {
        lightColorScheme(
            primary = palette.primary,
            onPrimary = Color.White,
            primaryContainer = palette.primarySoft,
            secondary = palette.accent,
            tertiary = palette.gold,
            background = palette.bg,
            onBackground = palette.text,
            surface = palette.surface,
            onSurface = palette.text,
            surfaceVariant = palette.surfaceAlt,
            onSurfaceVariant = palette.textSecondary,
            outline = palette.border,
            outlineVariant = palette.borderStrong,
            error = palette.danger
        )
    }

    CompositionLocalProvider(
        LocalXPhotosPalette provides palette,
        LocalAnimScale provides animationSpeedScale
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = XPhotosTypography,
            shapes = XPhotosShapes,
            content = content
        )
    }
}

val MaterialTheme.xPalette: XPhotosPalette
    @Composable
    @ReadOnlyComposable
    get() = LocalXPhotosPalette.current
