package dev.hgokhale.lysta

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/*
Surface – A role used for backgrounds and large, low-emphasis areas of the screen.
Primary, Secondary, Tertiary – Accent color roles used to emphasize or de-emphasize foreground elements.
Container – Roles used as a fill color for foreground elements like buttons. They should not be used for text or icons.

Primary = important actions and elements needing the most emphasis, like a FAB to start a new message.
Secondary = elements that don’t need immediate attention and don’t need emphasis, like the selected state of a navigation icon or a dismissive button.
Tertiary = smaller elements that need special emphasis but don't require immediate attention, such as a badge or notification.
inverseSurface = used for snackbars, dialogs, and other surfaces that need to be on top of the background.
*/

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
        onSurface = Color.Black,
        inverseSurface = Color(0xFFF3F3F3),
        inverseOnSurface = Color(0xFF000000),
        inversePrimary = Color(0xFF5DA3C9),
    )

    val darkColors = darkColorScheme(
        primary = Color(0xFFA5D3EC),
        secondary = Color(0xFFFFFFFF),
        background = Color(0xFF000000),
        surface = Color(0xFF0000000),
        onPrimary = Color.Black,
        onSecondary = Color(0x77FFFFFF),
        onBackground = Color.White,
        onSurface = Color.White,
        inverseSurface = Color(0xFF1F1F1F),
        inverseOnSurface = Color(0xFFFFFFFF),
        inversePrimary = Color(0xFFA5D3EC),
    )

    MaterialTheme(
        colorScheme = if (darkTheme) darkColors else lightColors,
        typography = Typography(),
        shapes = Shapes(),
        content = content
    )
}

