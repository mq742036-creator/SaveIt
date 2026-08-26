package com.saveit.downloader.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

@Composable
fun SaveItTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = SaveItPrimary,
            secondary = SaveItSecondary,
            tertiary = SaveItAccent,
            background = SaveItBackground,
            surface = SaveItSurface,
            surfaceVariant = SaveItSurfaceVariant,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onTertiary = Color.White,
            onBackground = SaveItText,
            onSurface = SaveItText,
        )
    } else {
        lightColorScheme(
            primary = SaveItPrimary,
            secondary = SaveItSecondary,
            tertiary = SaveItAccent,
            background = Color(0xFFF5F6FA),
            surface = Color.White,
            surfaceVariant = Color(0xFFE8E8F0),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onTertiary = Color.White,
            onBackground = Color(0xFF1A1A2E),
            onSurface = Color(0xFF1A1A2E),
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(
            headlineLarge = MaterialTheme.typography.headlineLarge.copy(
                fontFamily = FontFamily.SansSerif
            ),
            headlineMedium = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = FontFamily.SansSerif
            ),
            titleLarge = MaterialTheme.typography.titleLarge.copy(
                fontFamily = FontFamily.SansSerif
            ),
            bodyLarge = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = FontFamily.SansSerif
            ),
        ),
        content = content
    )
}
