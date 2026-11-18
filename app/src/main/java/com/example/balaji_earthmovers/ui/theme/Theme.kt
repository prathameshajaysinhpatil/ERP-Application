package com.example.balaji_earthmovers.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BrandGoldSecondary,
    onPrimary = Color(0xFF1F1200),
    primaryContainer = BrandGoldDark,
    onPrimaryContainer = Color(0xFFFEF3C7),
    secondary = InfoBlue,
    onSecondary = Color.White,
    secondaryContainer = SlateNavyElevated,
    onSecondaryContainer = Color(0xFFE2E8F0),
    tertiary = BrandGoldPrimary,
    onTertiary = Color.White,
    background = SlateNavyDark,
    onBackground = TextPrimaryDark,
    surface = SlateNavySurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = SlateNavyElevated,
    onSurfaceVariant = TextSecondaryDark,
    outline = SlateNavyBorder,
    outlineVariant = Color(0xFF1E293B),
    error = ExpenseRedDark,
    onError = Color.White,
    errorContainer = Color(0xFF450A0A),
    onErrorContainer = Color(0xFFFEE2E2)
)

private val LightColorScheme = lightColorScheme(
    primary = BrandGoldPrimary,
    onPrimary = Color.White,
    primaryContainer = BrandGoldLightWash,
    onPrimaryContainer = Color(0xFF78350F),
    secondary = Color(0xFF1E293B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF1F5F9),
    onSecondaryContainer = Color(0xFF0F172A),
    tertiary = InfoBlue,
    onTertiary = Color.White,
    background = SlateCanvasLight,
    onBackground = TextPrimaryLight,
    surface = SlateSurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = TextSecondaryLight,
    outline = SlateLightBorder,
    outlineVariant = Color(0xFFE2E8F0),
    error = ExpenseRed,
    onError = Color.White,
    errorContainer = ExpenseRedLight,
    onErrorContainer = Color(0xFF7F1D1D)
)

@Composable
fun BalajiEarthmoversTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
