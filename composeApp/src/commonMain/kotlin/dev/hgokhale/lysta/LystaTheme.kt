package dev.hgokhale.lysta

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import lysta.composeapp.generated.resources.Res
import lysta.composeapp.generated.resources.inter
import lysta.composeapp.generated.resources.inter_italic
import org.jetbrains.compose.resources.Font

@Composable
fun LystaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val (lightColors, darkColors) = lystaColorSchemes()
    MaterialTheme(
        colorScheme = if (darkTheme) darkColors else lightColors,
        typography = lystaTypography(),
        shapes = Shapes(),
        content = content
    )
}

/*
Surface – A role used for backgrounds and large, low-emphasis areas of the screen.
Primary, Secondary, Tertiary – Accent color roles used to emphasize or de-emphasize foreground elements.
Container – Roles used as a fill color for foreground elements like buttons. They should not be used for text or icons.

Primary = important actions and elements needing the most emphasis, like a FAB to start a new message.
Secondary = elements that don’t need immediate attention and don’t need emphasis. We use this for most icons like checkboxes and drag bars.
Tertiary = smaller elements that need special emphasis but don't require immediate attention, such as a badge or notification.
inverseSurface = used for snackbars, dialogs, and other surfaces that need to be on top of the background.
*/
private fun lystaColorSchemes(): Pair<ColorScheme, ColorScheme> {
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
    return Pair(lightColors, darkColors)
}

@Composable
private fun lystaTypography(): Typography {
    val lystaFontFamily = FontFamily(
        Font(resource = Res.font.inter, style = FontStyle.Normal),
        Font(resource = Res.font.inter_italic, style = FontStyle.Italic)
    )

    val defaultTypography = Typography()
    val lystaTypography = Typography(
        displayLarge = defaultTypography.displayLarge.copy(fontFamily = lystaFontFamily),
        displayMedium = defaultTypography.displayMedium.copy(fontFamily = lystaFontFamily),
        displaySmall = defaultTypography.displaySmall.copy(fontFamily = lystaFontFamily),
        headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = lystaFontFamily),
        headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = lystaFontFamily),
        headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = lystaFontFamily),
        titleLarge = defaultTypography.titleLarge.copy(fontFamily = lystaFontFamily),
        titleMedium = defaultTypography.titleMedium.copy(fontFamily = lystaFontFamily),
        titleSmall = defaultTypography.titleSmall.copy(fontFamily = lystaFontFamily),
        bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = lystaFontFamily),
        bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = lystaFontFamily),
        bodySmall = defaultTypography.bodySmall.copy(fontFamily = lystaFontFamily),
        labelLarge = defaultTypography.labelLarge.copy(fontFamily = lystaFontFamily),
        labelMedium = defaultTypography.labelMedium.copy(fontFamily = lystaFontFamily),
        labelSmall = defaultTypography.labelSmall.copy(fontFamily = lystaFontFamily),
    )
    return lystaTypography
}

