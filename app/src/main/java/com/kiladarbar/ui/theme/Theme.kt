package com.kiladarbar.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Royal Mughal Color Palette
val RoyalMaroon       = Color(0xFF7C1D1D)
val RoyalMaroonDark   = Color(0xFF5A1A1A)
val RoyalMaroonLight  = Color(0xFFB12020)
val RoyalGold         = Color(0xFFF59E0B)
val RoyalGoldLight    = Color(0xFFFBBF24)
val RoyalGoldDark     = Color(0xFFD97706)
val RoyalCream        = Color(0xFFFFF8F0)
val RoyalDark         = Color(0xFF1A0A0A)
val RoyalDarkSurface  = Color(0xFF2D1414)
val RoyalDarkVariant  = Color(0xFF3C1919)

private val LightColorScheme = lightColorScheme(
    primary           = RoyalMaroon,
    onPrimary         = RoyalCream,
    primaryContainer  = Color(0xFFFCE4E4),
    onPrimaryContainer = RoyalMaroonDark,
    secondary         = RoyalGold,
    onSecondary       = RoyalDark,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = RoyalGoldDark,
    tertiary          = Color(0xFF6D3A2A),
    background        = RoyalCream,
    onBackground      = RoyalDark,
    surface           = Color.White,
    onSurface         = RoyalDark,
    surfaceVariant    = Color(0xFFF1EBE5),
    onSurfaceVariant  = Color(0xFF785050),
    error             = Color(0xFFB00020),
    outline           = Color(0xFFE7D4D4),
)

private val DarkColorScheme = darkColorScheme(
    primary           = RoyalGold,
    onPrimary         = RoyalDark,
    primaryContainer  = RoyalMaroonDark,
    onPrimaryContainer = RoyalCream,
    secondary         = RoyalMaroonLight,
    onSecondary       = RoyalCream,
    secondaryContainer = RoyalDarkVariant,
    onSecondaryContainer = Color(0xFFFFCCCC),
    tertiary          = RoyalGoldLight,
    background        = RoyalDark,
    onBackground      = RoyalCream,
    surface           = RoyalDarkSurface,
    onSurface         = RoyalCream,
    surfaceVariant    = RoyalDarkVariant,
    onSurfaceVariant  = Color(0xFFC8A0A0),
    error             = Color(0xFFCF6679),
    outline           = Color(0xFF501E1E),
)

@Composable
fun KilaDarbarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = KilaDarbarTypography,
        shapes = KilaDarbarShapes,
        content = content,
    )
}
