package com.kiladarbar.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kiladarbar.ui.theme.Gold
import com.kiladarbar.ui.theme.Maroon
import kotlinx.coroutines.isActive
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class Particle(
    var x: Float,
    var y: Float,
    val vx: Float,
    val vy: Float,
    val radius: Float,
    var alpha: Float,
    val alphaDecay: Float,
    val color: Color,
)

@Composable
fun GoldParticleCanvas(
    modifier: Modifier = Modifier,
    count: Int = 60,
    canvasWidth: Dp = 400.dp,
    canvasHeight: Dp = 400.dp,
) {
    val density = LocalDensity.current
    val w = with(density) { canvasWidth.toPx() }
    val h = with(density) { canvasHeight.toPx() }

    val particles = remember {
        mutableStateListOf<Particle>().also { list ->
            repeat(count) {
                list.add(randomParticle(w, h))
            }
        }
    }

    var frameTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        var lastTime = System.nanoTime()
        while (isActive) {
            withFrameNanos { nano ->
                val dt = (nano - lastTime) / 1_000_000_000f
                lastTime = nano
                particles.forEachIndexed { i, p ->
                    p.x    += p.vx * dt * 60f
                    p.y    += p.vy * dt * 60f
                    p.alpha -= p.alphaDecay * dt * 60f
                    if (p.alpha <= 0f || p.y < -20f || p.x < -20f || p.x > w + 20f) {
                        particles[i] = randomParticle(w, h)
                    } else {
                        particles[i] = p
                    }
                }
                frameTime = nano
            }
        }
    }

    Canvas(modifier = modifier) {
        particles.forEach { p ->
            drawCircle(
                color  = p.color.copy(alpha = p.alpha.coerceIn(0f, 0.8f)),
                radius = p.radius,
                center = Offset(p.x, p.y),
            )
            if (p.radius > 2f) {
                drawCircle(
                    color  = p.color.copy(alpha = p.alpha * 0.15f),
                    radius = p.radius * 3f,
                    center = Offset(p.x, p.y),
                )
            }
        }
    }
}

private fun randomParticle(w: Float, h: Float): Particle {
    val isGold = Random.nextFloat() > 0.25f
    val angle  = Random.nextFloat() * 360f
    val speed  = Random.nextFloat() * 0.4f + 0.1f
    return Particle(
        x          = Random.nextFloat() * w,
        y          = h + Random.nextFloat() * 20f,
        vx         = cos(Math.toRadians(angle.toDouble())).toFloat() * speed,
        vy         = -(sin(Math.toRadians(angle.toDouble())).toFloat().coerceAtLeast(0.1f) * speed + 0.3f),
        radius     = Random.nextFloat() * 2.5f + 0.5f,
        alpha      = Random.nextFloat() * 0.6f + 0.2f,
        alphaDecay = Random.nextFloat() * 0.003f + 0.001f,
        color      = if (isGold) Gold else Maroon,
    )
}
