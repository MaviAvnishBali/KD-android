package com.kiladarbar.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

/* ── Spacing ── */
object Spacing {
    val xs  = 4.dp
    val sm  = 8.dp
    val md  = 16.dp
    val lg  = 24.dp
    val xl  = 32.dp
    val xxl = 48.dp
}

/* ── Elevation ── */
object Elevation {
    val card    = 0.dp
    val overlay = 8.dp
    val modal   = 24.dp
}

/* ── Easing curves ── */
object Easing {
    val ExpoOut  = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
    val BackOut  = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)
    val Standard = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
}

/* ── Animation specs ── */
object Motion {
    val SpringFast   = spring<Float>(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
    val SpringMedium = spring<Float>(dampingRatio = Spring.DampingRatioLowBouncy,    stiffness = Spring.StiffnessLow)
    val SpringSlow   = spring<Float>(dampingRatio = 0.7f, stiffness = 200f)
    val TweenFast    = tween<Float>(durationMillis = 300, easing = Easing.ExpoOut)
    val TweenMedium  = tween<Float>(durationMillis = 500, easing = Easing.ExpoOut)
    val TweenSlow    = tween<Float>(durationMillis = 800, easing = Easing.ExpoOut)
}

/* ── Gradients ── */
object LuxuryGradients {
    val goldHorizontal = Brush.horizontalGradient(listOf(Gold, GoldDark))
    val goldVertical   = Brush.verticalGradient(listOf(Gold, GoldDark))
    val maroonVertical = Brush.verticalGradient(listOf(Maroon, MaroonDark))
    val darkRadial     = Brush.radialGradient(
        colors  = listOf(MaroonDark, Obsidian),
        radius  = 800f,
    )
    val heroOverlay    = Brush.verticalGradient(
        colorStops = arrayOf(
            0f   to androidx.compose.ui.graphics.Color.Transparent,
            0.5f to Obsidian.copy(alpha = 0.5f),
            1f   to Obsidian,
        )
    )
    val cardOverlay    = Brush.verticalGradient(
        colorStops = arrayOf(
            0f   to androidx.compose.ui.graphics.Color.Transparent,
            1f   to Obsidian.copy(alpha = 0.9f),
        )
    )
    val shimmer        = Brush.horizontalGradient(
        colors = listOf(
            Surface1,
            Surface2,
            Surface1,
        )
    )
}
