package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val NinjaDarkColorScheme = darkColorScheme(
    primary = NinjaPrimary,
    onPrimary = Color.White,
    primaryContainer = NinjaPrimaryContainer,
    onPrimaryContainer = NinjaOnPrimaryContainer,
    secondary = NinjaSecondary,
    onSecondary = Color.Black,
    secondaryContainer = NinjaSecondaryContainer,
    onSecondaryContainer = NinjaOnSecondaryContainer,
    tertiary = NinjaAccentCyan,
    background = NinjaBackgroundDark,
    onBackground = Color(0xFFF1F5F9),
    surface = NinjaSurfaceDark,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = NinjaCardDark,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = NinjaBorderDark
)

private val NinjaLightColorScheme = lightColorScheme(
    primary = Color(0xFF7C3AED),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = Color(0xFF4C1D95),
    secondary = Color(0xFF0891B2),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFFAFE),
    onSecondaryContainer = Color(0xFF164E63),
    tertiary = Color(0xFF6366F1),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1)
)

@Composable
fun NinjaTheme(
    darkTheme: Boolean = true, // Default to privacy-first dark stealth theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) NinjaDarkColorScheme else NinjaLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            var currentContext = view.context
            while (currentContext is android.content.ContextWrapper) {
                if (currentContext is Activity) break
                currentContext = currentContext.baseContext
            }
            val activity = currentContext as? Activity
            activity?.window?.let { window ->
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Alias for backwards compatibility with template
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    NinjaTheme(darkTheme = darkTheme, content = content)
}
