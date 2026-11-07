package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = RoyalBlueLight,
    onPrimary = Color.White,
    primaryContainer = NavyLight,
    onPrimaryContainer = ElectricCyan,
    secondary = IndigoPrimary,
    onSecondary = Color.White,
    secondaryContainer = NavyDeep,
    onSecondaryContainer = SoftPurple,
    tertiary = ElectricCyan,
    onTertiary = NavyDark,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = CardBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = NavyDeep,
    onPrimary = Color.White,
    primaryContainer = RoyalBlue,
    onPrimaryContainer = Color.White,
    secondary = RoyalBlue,
    onSecondary = Color.White,
    secondaryContainer = SurfaceVariantLight,
    onSecondaryContainer = NavyDeep,
    tertiary = IndigoPrimary,
    onTertiary = Color.White,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = CardBorderLight
)

@Composable
fun CapAiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep intentional brand design identity
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
