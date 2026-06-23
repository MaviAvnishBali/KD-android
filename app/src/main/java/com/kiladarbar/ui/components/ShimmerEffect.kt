package com.kiladarbar.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kiladarbar.ui.theme.*

@Composable
fun shimmerBrush(targetValue: Float = 1000f): Brush {
    val shimmerColors = listOf(
        Surface1,
        Surface2,
        Color(0xFF2E1618),
        Surface2,
        Surface1,
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue  = targetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerTranslate",
    )

    return Brush.linearGradient(
        colors      = shimmerColors,
        start       = Offset.Zero,
        end         = Offset(x = translateAnim, y = translateAnim),
    )
}

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp),
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(shimmerBrush())
    )
}

@Composable
fun HomeScreenShimmer() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Hero shimmer
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            shape = RoundedCornerShape(20.dp),
        )

        // Quick actions shimmer
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(4) {
                ShimmerBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp),
                    shape = RoundedCornerShape(14.dp),
                )
            }
        }

        // Section title shimmer
        ShimmerBox(modifier = Modifier.width(160.dp).height(20.dp))

        // Card row shimmer
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(2) {
                ShimmerBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(180.dp),
                    shape = RoundedCornerShape(16.dp),
                )
            }
        }

        // List item shimmers
        repeat(3) {
            MenuItemShimmer()
        }
    }
}

@Composable
fun MenuItemShimmer() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ShimmerBox(
            modifier = Modifier.size(88.dp),
            shape = RoundedCornerShape(14.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.7f).height(16.dp))
            ShimmerBox(modifier = Modifier.fillMaxWidth().height(12.dp))
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.5f).height(12.dp))
            ShimmerBox(modifier = Modifier.width(60.dp).height(20.dp))
        }
    }
}
