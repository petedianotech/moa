package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MoaDarkColorScheme = darkColorScheme(
    primary = RoyalPurple,
    onPrimary = Color.White,
    secondary = DeepBlue,
    onSecondary = Color.White,
    tertiary = VibrantGold,
    background = DarkSpaceBackground,
    surface = SurfaceFrostedGlass,
    onBackground = Color.White,
    onSurface = Color.White,
    error = RichRed
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MoaDarkColorScheme,
        typography = Typography,
        content = content
    )
}
