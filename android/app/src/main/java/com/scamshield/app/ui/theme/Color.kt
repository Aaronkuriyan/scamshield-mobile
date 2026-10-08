package com.scamshield.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// --- Base Color Palette ---
// Dark Palette (Cybersecurity Neutral Dark)
val DarkBackground = Color(0xFF0F1115)
val DarkSurface = Color(0xFF171A21)
val DarkSurfaceElevated = Color(0xFF1E232E)
val DarkBorder = Color(0xFF262E3D)
val DarkTextPrimary = Color(0xFFF8FAFC)
val DarkTextSecondary = Color(0xFF94A3B8)
val DarkPrimaryNavy = Color(0xFF0A192F)

// Light Palette (Soft Off-White / Light Cool Neutral #F5F7FA)
val LightBackground = Color(0xFFF5F7FA)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceElevated = Color(0xFFEEF2F6)
val LightBorder = Color(0xFFE2E8F0)
val LightTextPrimary = Color(0xFF0F172A)
val LightTextSecondary = Color(0xFF64748B)
val LightPrimaryNavy = Color(0xFF0F2744)

// Brand & Semantic Accents
val AccentEmerald = Color(0xFF10B981)
val AccentEmeraldDark = Color(0xFF064E3B)
val AccentEmeraldLight = Color(0xFFECFDF5)
val AccentEmeraldBorder = Color(0xFFA7F3D0)

val AlertCrimson = Color(0xFFEF4444)
val AlertCrimsonDark = Color(0xFF7F1D1D)
val AlertCrimsonLight = Color(0xFFFEF2F2)
val AlertCrimsonBorder = Color(0xFFFECACA)

val CautionAmber = Color(0xFFF59E0B)
val CautionAmberDark = Color(0xFF78350F)
val CautionAmberLight = Color(0xFFFFFBEB)
val CautionAmberBorder = Color(0xFFFDE68A)

// Compatibility Fallbacks
val SurfaceDark = DarkBackground
val PrimaryNavy = DarkPrimaryNavy
val CardNavy = DarkSurface
val CardNavyElevated = DarkSurfaceElevated
val TextWhite = DarkTextPrimary
val TextMuted = DarkTextSecondary
val DividerColor = DarkBorder
val CardNavyBorder = DarkBorder

// --- Semantic Theme Tokens ---
data class ScamShieldColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val brandNavy: Color,
    val accentEmerald: Color,
    val accentEmeraldBg: Color,
    val accentEmeraldBorder: Color,
    val alertCrimson: Color,
    val alertCrimsonBg: Color,
    val alertCrimsonBorder: Color,
    val cautionAmber: Color,
    val cautionAmberBg: Color,
    val cautionAmberBorder: Color,
    val inputBackground: Color,
    val divider: Color,
    val primary: Color = brandNavy
)

val DarkThemeColors = ScamShieldColors(
    isDark = true,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceElevated = DarkSurfaceElevated,
    border = DarkBorder,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    brandNavy = DarkPrimaryNavy,
    accentEmerald = AccentEmerald,
    accentEmeraldBg = AccentEmeraldDark.copy(alpha = 0.35f),
    accentEmeraldBorder = AccentEmerald.copy(alpha = 0.5f),
    alertCrimson = AlertCrimson,
    alertCrimsonBg = AlertCrimsonDark.copy(alpha = 0.35f),
    alertCrimsonBorder = AlertCrimson.copy(alpha = 0.5f),
    cautionAmber = CautionAmber,
    cautionAmberBg = CautionAmberDark.copy(alpha = 0.35f),
    cautionAmberBorder = CautionAmber.copy(alpha = 0.5f),
    inputBackground = DarkSurfaceElevated,
    divider = DarkBorder
)

val LightThemeColors = ScamShieldColors(
    isDark = false,
    background = LightBackground,
    surface = LightSurface,
    surfaceElevated = LightSurfaceElevated,
    border = LightBorder,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    brandNavy = LightPrimaryNavy,
    accentEmerald = AccentEmerald,
    accentEmeraldBg = AccentEmeraldLight,
    accentEmeraldBorder = AccentEmeraldBorder,
    alertCrimson = AlertCrimson,
    alertCrimsonBg = AlertCrimsonLight,
    alertCrimsonBorder = AlertCrimsonBorder,
    cautionAmber = CautionAmber,
    cautionAmberBg = CautionAmberLight,
    cautionAmberBorder = CautionAmberBorder,
    inputBackground = LightSurfaceElevated,
    divider = LightBorder
)

val LocalScamShieldColors = staticCompositionLocalOf { DarkThemeColors }

object AppTheme {
    val colors: ScamShieldColors
        @Composable
        @ReadOnlyComposable
        get() = LocalScamShieldColors.current
}
