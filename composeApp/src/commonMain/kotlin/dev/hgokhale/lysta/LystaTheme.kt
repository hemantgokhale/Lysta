package dev.hgokhale.lysta

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun LystaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val lightColors = lightColorScheme(
        primary = Color(0xFF5DA3C9),
        secondary = Color(0xFF000000),
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFFFFFFF),
        onPrimary = Color.White,
        onSecondary = Color(0x77000000),
        onBackground = Color.Black,
        onSurface = Color.Black
    )

    val darkColors = darkColorScheme(
        primary = Color(0xFFA5D3EC),
        secondary = Color(0xFFFFFFFF),
        background = Color(0xFF000000),
        surface = Color(0xFF0000000),
        onPrimary = Color.Black,
        onSecondary = Color(0x77FFFFFF),
        onBackground = Color.White,
        onSurface = Color.White
    )

    MaterialTheme(
        colorScheme = if (darkTheme) darkColors else lightColors,
        typography = Typography(),
        shapes = Shapes(),
        content = content
    )
}

