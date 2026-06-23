package com.kiladarbar.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kiladarbar.ui.components.GoldParticleCanvas
import com.kiladarbar.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {

    /* ── Animation values ── */
    val bgAlpha      = remember { Animatable(0f) }
    val sealScale    = remember { Animatable(0.4f) }
    val sealAlpha    = remember { Animatable(0f) }
    val sealRotation = remember { Animatable(-30f) }
    val ringScale1   = remember { Animatable(0.6f) }
    val ringScale2   = remember { Animatable(0.3f) }
    val ringAlpha1   = remember { Animatable(0f) }
    val ringAlpha2   = remember { Animatable(0f) }
    val titleAlpha   = remember { Animatable(0f) }
    val titleY       = remember { Animatable(32f) }
    val taglineAlpha = remember { Animatable(0f) }
    val dividerScaleX = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Background fade in
        bgAlpha.animateTo(1f, tween(400))

        // Ring reveals
        ringAlpha1.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
        ringScale1.animateTo(1f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow))

        delay(100)
        ringAlpha2.animateTo(0.5f, tween(400))
        ringScale2.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow))

        // Seal enter
        sealAlpha.animateTo(1f, tween(500))
        sealScale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow))
        sealRotation.animateTo(0f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow))

        delay(200)

        // Title reveal
        titleAlpha.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
        titleY.animateTo(0f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMediumLow))

        delay(100)

        // Divider and tagline
        dividerScaleX.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
        taglineAlpha.animateTo(1f, tween(400))

        delay(1800)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
            .alpha(bgAlpha.value),
        contentAlignment = Alignment.Center,
    ) {
        /* ── Gold particles background ── */
        GoldParticleCanvas(
            modifier      = Modifier.fillMaxSize(),
            count         = 50,
            canvasWidth   = 400.dp,
            canvasHeight  = 800.dp,
        )

        /* ── Radial glow ── */
        Box(
            modifier = Modifier
                .size(500.dp)
                .align(Alignment.Center)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Maroon.copy(alpha = 0.35f),
                            Obsidian.copy(alpha = 0f),
                        )
                    )
                )
        )

        /* ── Outer ring ── */
        Box(
            modifier = Modifier
                .size(260.dp)
                .scale(ringScale1.value)
                .alpha(ringAlpha1.value)
                .clip(CircleShape)
                .background(Color.Transparent)
                .align(Alignment.Center),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors  = listOf(Maroon.copy(alpha = 0.2f), Color.Transparent),
                        )
                    )
            )
        }

        /* ── Inner ring ── */
        Box(
            modifier = Modifier
                .size(200.dp)
                .scale(ringScale2.value)
                .alpha(ringAlpha2.value)
                .clip(CircleShape)
                .background(GoldBorder)
                .align(Alignment.Center),
        )

        /* ── Main content ── */
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            /* Seal */
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(sealScale.value)
                    .alpha(sealAlpha.value)
                    .graphicsLayer { rotationZ = sealRotation.value }
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(GoldDark, Gold, GoldLight),
                            center = Offset(70f, 50f),
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text       = "KD",
                        fontSize   = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Obsidian,
                        textAlign  = TextAlign.Center,
                        letterSpacing = 2.sp,
                    )
                }
            }

            /* Title + divider + tagline */
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text          = "KILA DARBAR",
                    fontSize      = 32.sp,
                    fontWeight    = FontWeight.Bold,
                    color         = Ivory,
                    letterSpacing = 6.sp,
                    modifier      = Modifier
                        .alpha(titleAlpha.value)
                        .offset(y = titleY.value.dp),
                )

                /* Animated gold divider */
                Row(
                    modifier              = Modifier
                        .scale(scaleX = dividerScaleX.value, scaleY = 1f)
                        .alpha(dividerScaleX.value),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(modifier = Modifier.width(60.dp).height(1.dp).background(Gold.copy(alpha = 0.6f)))
                    Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(Gold))
                    Box(modifier = Modifier.width(60.dp).height(1.dp).background(Gold.copy(alpha = 0.6f)))
                }

                Text(
                    text          = "ROYAL MUGHAL CUISINE",
                    fontSize      = 11.sp,
                    fontWeight    = FontWeight.Medium,
                    color         = IvoryDim.copy(alpha = 0.7f),
                    letterSpacing = 3.sp,
                    modifier      = Modifier.alpha(taglineAlpha.value),
                )
            }
        }
    }
}
