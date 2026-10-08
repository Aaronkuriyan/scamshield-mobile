package com.scamshield.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AccentEmerald,
    secondary = CautionAmber,
    tertiary = AlertCrimson,
    background = SurfaceDark,
    surface = CardNavy,
    onPrimary = SurfaceDark,
    onSecondary = SurfaceDark,
    onTertiary = TextWhite,
    onBackground = TextWhite,
    onSurface = TextWhite
)

@Composable
fun SCAMSHIELDTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
