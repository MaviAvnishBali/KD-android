package com.kiladarbar.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kiladarbar.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val logoScale  = remember { Animatable(0.7f) }
    val logoAlpha  = remember { Animatable(0f) }
    val textAlpha  = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        logoAlpha.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
        logoScale.animateTo(
            1f,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        )
        textAlpha.animateTo(1f, tween(500))
        delay(1800)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(RoyalMaroon, RoyalMaroonDark, RoyalDark))),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(horizontal = 32.dp),
        ) {
            // Gold circle with app name
            Box(
                modifier = Modifier
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(RoyalGold),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Kila\nDarbar",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = RoyalDark,
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp,
                )
            }

            // App name + divider + tagline
            Column(
                modifier = Modifier.alpha(textAlpha.value),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "Kila Darbar",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = RoyalGold,
                    letterSpacing = 1.sp,
                )

                // Decorative divider
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(modifier = Modifier.width(48.dp).height(1.dp).background(RoyalGold.copy(alpha = 0.5f)))
                    Box(modifier = Modifier.size(4.dp).clip(RoundedCornerShape(2.dp)).background(RoyalGold))
                    Box(modifier = Modifier.width(48.dp).height(1.dp).background(RoyalGold.copy(alpha = 0.5f)))
                }

                Text(
                    text = "Royal Mughal Cuisine",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.65f),
                    letterSpacing = 2.sp,
                )
            }
        }
    }
}
