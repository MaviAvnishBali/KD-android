package com.kiladarbar.ui.screens.orders

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.kiladarbar.ui.components.*
import com.kiladarbar.ui.theme.*
import kotlinx.coroutines.delay

private data class TrackStep(val status: String, val label: String, val emoji: String, val detail: String)

private val trackingSteps = listOf(
    TrackStep("PENDING",          "Order Placed",      "📋", "We received your order"),
    TrackStep("CONFIRMED",        "Confirmed",         "✅", "Restaurant accepted your order"),
    TrackStep("PREPARING",        "Preparing",         "👨‍🍳", "Our chefs are crafting your meal"),
    TrackStep("READY",            "Ready for Pickup",  "🍽️", "Your royal feast is ready"),
    TrackStep("OUT_FOR_DELIVERY", "On the Way",        "🛵", "Your order is heading to you"),
    TrackStep("DELIVERED",        "Delivered!",        "👑", "Enjoy your royal meal"),
)

@Composable
fun OrderTrackingScreen(
    orderId: String,
    onBack: () -> Unit,
    viewModel: OrderTrackingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(orderId) {
        viewModel.loadOrder(orderId)
        viewModel.startTracking(orderId)
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.stopTracking() }
    }

    // Demo: progress through states
    var demoStep by remember { mutableIntStateOf(1) }
    LaunchedEffect(Unit) {
        while (demoStep < trackingSteps.size) {
            delay(2500)
            demoStep++
        }
    }

    val currentStatus = uiState.status ?: trackingSteps.getOrNull(demoStep - 1)?.status ?: "CONFIRMED"

    Scaffold(
        containerColor = Obsidian,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null, tint = Ivory) }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TRACKING ORDER", style = MaterialTheme.typography.titleSmall, color = Ivory, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                    Text("#${orderId.take(8).uppercase()}", style = MaterialTheme.typography.labelSmall, color = Gold)
                }
                IconButton(onClick = {}) { Icon(Icons.Filled.Call, null, tint = Gold) }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ETA pulse card
            EtaCard(currentIndex = demoStep - 1)

            // Status timeline
            OrderTimeline(currentIndex = demoStep - 1)

            // Driver card
            DriverCard()

            // Order summary
            OrderSummaryCard()

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun EtaCard(currentIndex: Int) {
    val step = trackingSteps.getOrElse(currentIndex) { trackingSteps.last() }
    val isDelivered = currentIndex >= trackingSteps.size - 1

    val pulseAlpha by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.3f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label        = "pulseAlpha",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (isDelivered)
                    Brush.horizontalGradient(listOf(Color(0xFF1B4332), Color(0xFF2D6A4F)))
                else
                    Brush.horizontalGradient(listOf(MaroonDark, Maroon))
            )
            .border(1.dp, if (isDelivered) Color(0xFF4CAF50).copy(0.4f) else GoldBorder, RoundedCornerShape(24.dp))
            .padding(20.dp),
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                EyebrowLabel(if (isDelivered) "Delivered!" else "Estimated Arrival")
                Text(
                    text       = if (isDelivered) "Enjoy your meal 🎉" else "~${(trackingSteps.size - currentIndex) * 5} min",
                    style      = MaterialTheme.typography.headlineSmall,
                    color      = Ivory,
                    fontWeight = FontWeight.Bold,
                )
                AnimatedContent(targetState = step.label, label = "statusLabel") { s ->
                    Text(s, style = MaterialTheme.typography.bodySmall, color = IvoryDim.copy(0.7f))
                }
            }
            Text(
                text     = step.emoji,
                fontSize = 52.sp,
                modifier = Modifier.alpha(if (isDelivered) 1f else pulseAlpha),
            )
        }
    }
}

@Composable
private fun OrderTimeline(currentIndex: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Surface1)
            .border(1.dp, GoldBorder, RoundedCornerShape(20.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        Text("Order Progress", style = MaterialTheme.typography.titleMedium, color = Ivory, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(16.dp))

        trackingSteps.forEachIndexed { i, step ->
            val isDone    = i < currentIndex
            val isCurrent = i == currentIndex

            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Icon column
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Step dot
                    val dotScale by animateFloatAsState(
                        targetValue  = if (isCurrent) 1.2f else 1f,
                        animationSpec = spring(Spring.DampingRatioMediumBouncy),
                        label        = "dotScale",
                    )
                    val dotBrush: Brush = when {
                        isDone    -> LuxuryGradients.maroonVertical
                        isCurrent -> LuxuryGradients.goldVertical
                        else      -> Brush.linearGradient(listOf(Surface2, Surface2))
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .scale(dotScale)
                            .clip(CircleShape)
                            .background(dotBrush)
                            .border(1.dp, if (isCurrent) Gold.copy(0.8f) else GoldBorder, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isDone) {
                            Icon(Icons.Filled.Check, null, tint = Ivory, modifier = Modifier.size(16.dp))
                        } else {
                            Text(step.emoji, fontSize = 14.sp)
                        }
                    }

                    // Connector line
                    if (i < trackingSteps.size - 1) {
                        val lineAlpha by animateFloatAsState(
                            targetValue  = if (isDone) 1f else 0.2f,
                            animationSpec = tween(400),
                            label        = "lineAlpha",
                        )
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(36.dp)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            (if (isDone) Gold else GoldBorder).copy(lineAlpha),
                                            (if (isDone) GoldDark else Border).copy(lineAlpha),
                                        )
                                    )
                                ),
                        )
                    }
                }

                // Text column
                Column(modifier = Modifier.padding(top = 6.dp, bottom = if (i < trackingSteps.size - 1) 28.dp else 0.dp)) {
                    Text(
                        text       = step.label,
                        style      = MaterialTheme.typography.labelLarge,
                        color      = when { isCurrent -> Gold; isDone -> IvoryDim; else -> IvoryDim.copy(0.35f) },
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    )
                    Text(
                        text  = step.detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = IvoryDim.copy(if (isDone || isCurrent) 0.55f else 0.25f),
                        fontSize = 11.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun DriverCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Surface1)
            .border(1.dp, GoldBorder, RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(LuxuryGradients.maroonVertical),
            contentAlignment = Alignment.Center,
        ) { Text("🛵", fontSize = 24.sp) }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Rahul Kumar", style = MaterialTheme.typography.titleSmall, color = Ivory, fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Filled.Star, null, tint = Gold, modifier = Modifier.size(12.dp))
                Text("4.8 · KA 05 MN 1234", style = MaterialTheme.typography.bodySmall, color = IvoryDim.copy(0.6f))
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(Maroon.copy(0.3f)).clickable {},
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Filled.Call, null, tint = Ivory, modifier = Modifier.size(18.dp)) }
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(Gold.copy(0.15f)).clickable {},
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Filled.Chat, null, tint = Gold, modifier = Modifier.size(18.dp)) }
        }
    }
}

@Composable
private fun OrderSummaryCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Surface1)
            .border(1.dp, GoldBorder, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("Order Summary", style = MaterialTheme.typography.titleMedium, color = Ivory, fontWeight = FontWeight.SemiBold)
        GoldDivider()
        listOf(Triple("🍚 Veg Dum Biryani × 1", "₹420", false), Triple("🧀 Paneer Tikka × 2", "₹520", false)).forEach { (name, price, _) ->
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text(name, style = MaterialTheme.typography.bodySmall, color = IvoryDim.copy(0.7f))
                Text(price, style = MaterialTheme.typography.labelMedium, color = Ivory, fontWeight = FontWeight.SemiBold)
            }
        }
        GoldDivider()
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Text("TOTAL", style = MaterialTheme.typography.labelLarge, color = Ivory, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Text("₹940", style = MaterialTheme.typography.titleMedium, color = Gold, fontWeight = FontWeight.Bold)
        }
    }
}
