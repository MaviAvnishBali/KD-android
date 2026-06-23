package com.kiladarbar.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/* ── Brand colors ── */
val Gold        = Color(0xFFD4AF37)
val GoldLight   = Color(0xFFF5E07A)
val GoldDark    = Color(0xFF9A7D20)
val Maroon      = Color(0xFF6B0F1A)
val MaroonDark  = Color(0xFF3D0409)
val MaroonLight = Color(0xFF9A1528)
val Obsidian    = Color(0xFF111111)
val Surface1    = Color(0xFF1A0A0B)
val Surface2    = Color(0xFF220C0F)
val Surface3    = Color(0xFF2C1015)
val Ivory       = Color(0xFFF8F4E9)
val IvoryDim    = Color(0xFFC8BFA8)
val Border      = Color(0xFF2A1215)
val GoldBorder  = Color(0x33D4AF37)

/* ── Legacy aliases kept for back-compat with existing screens ── */
val RoyalMaroon       = Maroon
val RoyalMaroonDark   = MaroonDark
val RoyalMaroonLight  = MaroonLight
val RoyalGold         = Gold
val RoyalGoldLight    = GoldLight
val RoyalGoldDark     = GoldDark
val RoyalCream        = Ivory
val RoyalDark         = Obsidian
val RoyalDarkSurface  = Surface1
val RoyalDarkVariant  = Surface2

/* ── Always-dark luxury scheme ── */
private val LuxuryDarkScheme = darkColorScheme(
    primary             = Gold,
    onPrimary           = Obsidian,
    primaryContainer    = Color(0xFF2A1C00),
    onPrimaryContainer  = GoldLight,
    secondary           = Maroon,
    onSecondary         = Ivory,
    secondaryContainer  = Color(0xFF3D0409),
    onSecondaryContainer = Color(0xFFFFCCCC),
    tertiary            = GoldLight,
    onTertiary          = Obsidian,
    background          = Obsidian,
    onBackground        = Ivory,
    surface             = Surface1,
    onSurface           = Ivory,
    surfaceVariant      = Surface2,
    onSurfaceVariant    = IvoryDim,
    surfaceTint         = Color(0x0AD4AF37),
    error               = Color(0xFFCF6679),
    onError             = Obsidian,
    outline             = Border,
    outlineVariant      = GoldBorder,
    scrim               = Color(0xCC000000),
    inverseSurface      = Ivory,
    inverseOnSurface    = Obsidian,
    inversePrimary      = MaroonDark,
)

data class LuxuryExtendedColors(
    val goldGradientStart: Color = Gold,
    val goldGradientEnd: Color   = GoldDark,
    val maroonGradientStart: Color = Maroon,
    val maroonGradientEnd: Color   = MaroonDark,
    val cardBorder: Color        = GoldBorder,
    val shimmerBase: Color       = Color(0xFF1E1010),
    val shimmerHighlight: Color  = Color(0xFF2A1818),
)

val LocalLuxuryColors = staticCompositionLocalOf { LuxuryExtendedColors() }

@Composable
fun KilaDarbarTheme(
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalLuxuryColors provides LuxuryExtendedColors()) {
        MaterialTheme(
            colorScheme = LuxuryDarkScheme,
            typography  = KilaDarbarTypography,
            shapes      = KilaDarbarShapes,
            content     = content,
        )
    }
}
