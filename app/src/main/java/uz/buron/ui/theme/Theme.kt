package uz.buron.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PurplePrimary,
    onPrimary = Color.White,
    primaryContainer = PurpleLight,
    onPrimaryContainer = PurpleDark,
    secondary = PurpleDark,
    onSecondary = Color.White,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    error = ErrorRed,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = PurpleLight,
    onPrimary = PurpleDark,
    primaryContainer = PurpleDark,
    onPrimaryContainer = PurpleLight,
    secondary = PurpleLight,
    onSecondary = PurpleDark,
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF1F5F9),
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun BuronTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = BuronTypography,
        content = content
    )
}
