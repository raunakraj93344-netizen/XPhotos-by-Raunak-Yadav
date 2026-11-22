package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.model.AccentPreset
import com.example.model.GradientKey

data class XPhotosPalette(
    val isDark: Boolean,
    val bg: Color,
    val bgElevated: Color,
    val surface: Color,
    val surfaceAlt: Color,
    val border: Color,
    val borderStrong: Color,
    val text: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val primary: Color,
    val primarySoft: Color,
    val accent: Color,
    val gold: Color,
    val danger: Color,
    val success: Color,
    val overlay: Color,
    val tabBar: Color
)

val DarkXPhotosPalette = XPhotosPalette(
    isDark = true,
    bg = Color(0xFF08080C),
    bgElevated = Color(0xFF0E0E15),
    surface = Color(0xFF14141D),
    surfaceAlt = Color(0xFF1D1D28),
    border = Color(0xFF262633),
    borderStrong = Color(0xFF343444),
    text = Color(0xFFF6F6FA),
    textSecondary = Color(0xFFB4B4C4),
    textMuted = Color(0xFF7C7C90),
    primary = Color(0xFF8B6BFF),
    primarySoft = Color(0x298B6BFF),
    accent = Color(0xFFFF5C8A),
    gold = Color(0xFFFFC24B),
    danger = Color(0xFFFF5D5D),
    success = Color(0xFF3DDC97),
    overlay = Color(0xB8040408),
    tabBar = Color(0xFA0C0C13)
)

val LightXPhotosPalette = XPhotosPalette(
    isDark = false,
    bg = Color(0xFFF6F6FA),
    bgElevated = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    surfaceAlt = Color(0xFFEDEDF4),
    border = Color(0xFFE3E3EC),
    borderStrong = Color(0xFFD2D2DE),
    text = Color(0xFF0B0B12),
    textSecondary = Color(0xFF52525F),
    textMuted = Color(0xFF8A8A99),
    primary = Color(0xFF6C4CFF),
    primarySoft = Color(0x1F6C4CFF),
    accent = Color(0xFFF43F72),
    gold = Color(0xFFD99A12),
    danger = Color(0xFFE23B3B),
    success = Color(0xFF12A56D),
    overlay = Color(0x8C08080E),
    tabBar = Color(0xFAFFFFFF)
)

fun buildPalette(
    useDark: Boolean,
    accentPreset: AccentPreset,
    pureAmoledDark: Boolean
): XPhotosPalette {
    val base = if (useDark) DarkXPhotosPalette else LightXPhotosPalette
    val primaryColor = Color(if (useDark) accentPreset.darkColorHex else accentPreset.lightColorHex)
    val primarySoftColor = primaryColor.copy(alpha = if (useDark) 0.16f else 0.12f)
    return if (useDark && pureAmoledDark) {
        base.copy(
            bg = Color.Black,
            bgElevated = Color(0xFF050508),
            surface = Color(0xFF0B0B10),
            surfaceAlt = Color(0xFF14141C),
            border = Color(0xFF1F1F29),
            borderStrong = Color(0xFF2E2E3C),
            primary = primaryColor,
            primarySoft = primarySoftColor,
            tabBar = Color(0xFF000000)
        )
    } else {
        base.copy(
            primary = primaryColor,
            primarySoft = primarySoftColor
        )
    }
}

object XPhotosGradients {
    val violet = listOf(Color(0xFF7C5CFF), Color(0xFFC05CFF))
    val sunset = listOf(Color(0xFFFF7A59), Color(0xFFFF3D77))
    val ocean = listOf(Color(0xFF2E7BFF), Color(0xFF21D4C4))
    val forest = listOf(Color(0xFF1F8A5B), Color(0xFF9BD14B))
    val ember = listOf(Color(0xFFFF9F1C), Color(0xFFFF4D4D))
    val midnight = listOf(Color(0xFF1E2A78), Color(0xFF5B2C83))
    val candy = listOf(Color(0xFFFF6FB5), Color(0xFF8B6BFF))
    val graphite = listOf(Color(0xFF2B2B38), Color(0xFF5C5C72))
    val searchTip = listOf(Color(0xFF221A44), Color(0xFF3A2060), Color(0xFF54265E))
    val secretBanner = listOf(Color(0xFF16132B), Color(0xFF2B1C4F), Color(0xFF1E2D4F))

    fun of(key: GradientKey): List<Color> = when (key) {
        GradientKey.violet -> violet
        GradientKey.sunset -> sunset
        GradientKey.ocean -> ocean
        GradientKey.forest -> forest
        GradientKey.ember -> ember
        GradientKey.midnight -> midnight
        GradientKey.candy -> candy
        GradientKey.graphite -> graphite
    }
}
