package com.kiladarbar.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Using system serif for display (Playfair-like feel) and default for body (clean Inter-like)
val DisplayFont = FontFamily.Serif
val BodyFont    = FontFamily.Default

val KilaDarbarTypography = Typography(
    displayLarge = TextStyle(
        fontFamily  = DisplayFont,
        fontWeight  = FontWeight.Bold,
        fontSize    = 57.sp,
        lineHeight  = 64.sp,
        letterSpacing = (-0.25).sp,
    ),
    displayMedium = TextStyle(
        fontFamily  = DisplayFont,
        fontWeight  = FontWeight.Bold,
        fontSize    = 45.sp,
        lineHeight  = 52.sp,
    ),
    displaySmall = TextStyle(
        fontFamily  = DisplayFont,
        fontWeight  = FontWeight.SemiBold,
        fontSize    = 36.sp,
        lineHeight  = 44.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily  = DisplayFont,
        fontWeight  = FontWeight.Bold,
        fontSize    = 32.sp,
        lineHeight  = 40.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily  = DisplayFont,
        fontWeight  = FontWeight.SemiBold,
        fontSize    = 28.sp,
        lineHeight  = 36.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily  = DisplayFont,
        fontWeight  = FontWeight.Medium,
        fontSize    = 24.sp,
        lineHeight  = 32.sp,
    ),
    titleLarge = TextStyle(
        fontFamily  = DisplayFont,
        fontWeight  = FontWeight.SemiBold,
        fontSize    = 22.sp,
        lineHeight  = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily  = BodyFont,
        fontWeight  = FontWeight.SemiBold,
        fontSize    = 16.sp,
        lineHeight  = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    titleSmall = TextStyle(
        fontFamily  = BodyFont,
        fontWeight  = FontWeight.Medium,
        fontSize    = 14.sp,
        lineHeight  = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily  = BodyFont,
        fontWeight  = FontWeight.Normal,
        fontSize    = 16.sp,
        lineHeight  = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily  = BodyFont,
        fontWeight  = FontWeight.Normal,
        fontSize    = 14.sp,
        lineHeight  = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontFamily  = BodyFont,
        fontWeight  = FontWeight.Normal,
        fontSize    = 12.sp,
        lineHeight  = 16.sp,
        letterSpacing = 0.4.sp,
    ),
    labelLarge = TextStyle(
        fontFamily  = BodyFont,
        fontWeight  = FontWeight.Medium,
        fontSize    = 14.sp,
        lineHeight  = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily  = BodyFont,
        fontWeight  = FontWeight.Medium,
        fontSize    = 12.sp,
        lineHeight  = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily  = BodyFont,
        fontWeight  = FontWeight.Medium,
        fontSize    = 11.sp,
        lineHeight  = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)
