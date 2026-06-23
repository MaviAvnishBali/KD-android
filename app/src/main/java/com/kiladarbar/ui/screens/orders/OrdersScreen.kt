package com.kiladarbar.ui.screens.orders

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.kiladarbar.ui.components.*
import com.kiladarbar.ui.models.UiOrder
import com.kiladarbar.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun OrdersScreen(
    onOrderClick: (String) -> Unit,
    onBack:       () -> Unit,
    onOrderNow:   () -> Unit,
    viewModel:    OrdersViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Obsidian,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Ivory)
                }
                Text(
                    "MY ORDERS",
                    style      = MaterialTheme.typography.titleSmall,
                    color      = Ivory,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.size(48.dp))
            }
        },
    ) { padding ->
        when {
            uiState.isLoading -> OrdersShimmer(Modifier.padding(padding))

            uiState.error != null && uiState.orders.isEmpty() ->
                ErrorState(uiState.error!!, viewModel::loadOrders, Modifier.padding(padding))

            uiState.orders.isEmpty() ->
                EmptyOrdersState(onOrderNow = onOrderNow, modifier = Modifier.padding(padding))

            else -> OrdersList(
                orders       = uiState.orders,
                onOrderClick = onOrderClick,
                modifier     = Modifier.padding(padding),
            )
        }
    }
}

/* ── Orders list ── */
@Composable
private fun OrdersList(
    orders:       List<UiOrder>,
    onOrderClick: (String) -> Unit,
    modifier:     Modifier = Modifier,
) {
    LazyColumn(
        modifier            = modifier.fillMaxSize().background(Obsidian),
        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Quick reorder banner
        item {
            QuickReorderBanner(order = orders.first(), onClick = { onOrderClick(orders.first().id) })
        }

        // Order cards
        items(orders, key = { it.id }) { order ->
            OrderCard(order = order, onClick = { onOrderClick(order.id) })
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

/* ── Quick reorder banner ── */
@Composable
private fun QuickReorderBanner(order: UiOrder, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Maroon,
                        0.6f to Color(0xFF3D0F1A),
                        1f to MaroonDark,
                    )
                )
            )
            .border(1.dp, GoldBorder, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(16.dp),
    ) {
        // Decorative circle
        Box(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 20.dp)
                .clip(CircleShape)
                .background(Gold.copy(0.05f)),
        )

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier              = Modifier.weight(1f),
            ) {
                // Emoji icon
                Box(
                    modifier         = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Gold.copy(0.15f))
                        .border(1.dp, GoldBorder, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(order.emoji, fontSize = 24.sp)
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    EyebrowLabel("Order Again")
                    Text(
                        text       = order.title,
                        style      = MaterialTheme.typography.titleSmall,
                        color      = Ivory,
                        fontWeight = FontWeight.SemiBold,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis,
                    )
                    Text(
                        text  = "₹%.0f · ${order.itemCount} items".format(order.totalAmount),
                        style = MaterialTheme.typography.labelSmall,
                        color = Gold,
                    )
                }
            }
            // Reorder chip
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(LuxuryGradients.goldHorizontal)
                    .clickable { onClick() }
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    "Reorder",
                    style      = MaterialTheme.typography.labelMedium,
                    color      = Obsidian,
                    fontWeight = FontWeight.Bold,
                )
                Icon(Icons.Filled.Refresh, null, tint = Obsidian, modifier = Modifier.size(13.dp))
            }
        }
    }
}

/* ── Order card ── */
@Composable
private fun OrderCard(order: UiOrder, onClick: () -> Unit) {
    val statusColor = when {
        order.isDelivered -> Color(0xFF4CAF50)
        order.isCancelled -> Color(0xFFEF5350)
        else              -> Color(0xFFFFA726)
    }
    val statusIcon = when {
        order.isDelivered -> "✓"
        order.isCancelled -> "✕"
        else              -> "⏳"
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(order.id) { delay(60); visible = true }

    AnimatedVisibility(
        visible = visible,
        enter   = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 3 },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Surface1)
                .border(1.dp, GoldBorder, RoundedCornerShape(20.dp))
                .clickable { onClick() }
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Header row
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top,
            ) {
                // Left: emoji + info
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier              = Modifier.weight(1f),
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.radialGradient(listOf(Maroon.copy(0.5f), Surface2))),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(order.emoji, fontSize = 28.sp)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text       = order.title,
                            style      = MaterialTheme.typography.titleSmall,
                            color      = Ivory,
                            fontWeight = FontWeight.SemiBold,
                            maxLines   = 1,
                            overflow   = TextOverflow.Ellipsis,
                        )
                        Text(
                            text  = "${order.itemCount} items",
                            style = MaterialTheme.typography.bodySmall,
                            color = IvoryDim.copy(0.55f),
                        )
                        Text(
                            text     = order.date,
                            style    = MaterialTheme.typography.labelSmall,
                            color    = IvoryDim.copy(0.35f),
                            fontSize = 10.sp,
                        )
                    }
                }
                // Right: amount + status
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text       = "₹%.0f".format(order.totalAmount),
                        style      = MaterialTheme.typography.titleSmall,
                        color      = Gold,
                        fontWeight = FontWeight.Bold,
                    )
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusColor.copy(0.12f))
                            .border(0.5.dp, statusColor.copy(0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(statusIcon, fontSize = 9.sp)
                        Text(
                            text       = order.status,
                            style      = MaterialTheme.typography.labelSmall,
                            color      = statusColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = 10.sp,
                        )
                    }
                }
            }

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, GoldBorder, GoldBorder, Color.Transparent)
                        )
                    )
            )

            // Action buttons
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Rate
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .border(0.5.dp, GoldBorder, RoundedCornerShape(10.dp))
                        .clickable { }
                        .padding(vertical = 9.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Star, null, tint = Gold, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Rate", style = MaterialTheme.typography.labelSmall, color = Gold, fontWeight = FontWeight.Medium)
                }
                // Reorder
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Gold.copy(0.13f))
                        .clickable { }
                        .padding(vertical = 9.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Refresh, null, tint = Gold, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Reorder", style = MaterialTheme.typography.labelSmall, color = Gold, fontWeight = FontWeight.Bold)
                }
                // Help
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .border(0.5.dp, GoldBorder, RoundedCornerShape(10.dp))
                        .clickable { }
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.HelpOutline, null, tint = IvoryDim.copy(0.5f), modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

/* ── Beautiful empty state ── */
@Composable
private fun EmptyOrdersState(onOrderNow: () -> Unit, modifier: Modifier = Modifier) {
    // Floating animation for the dish
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = -14f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label         = "floatY",
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.15f,
        targetValue   = 0.35f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label         = "glowAlpha",
    )

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); visible = true }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Obsidian)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(Modifier.height(32.dp))

        // Animated glow + floating dish
        AnimatedVisibility(visible, enter = fadeIn(tween(600)) + scaleIn(spring(Spring.DampingRatioLowBouncy), 0.6f)) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                // Outer glow
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Maroon.copy(glowAlpha), Color.Transparent)
                            )
                        )
                )
                // Inner ring
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(Surface1)
                        .border(1.dp, GoldBorder, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text     = "🍽️",
                        fontSize = 56.sp,
                        modifier = Modifier.offset(y = floatOffset.dp),
                    )
                }
                // Floating mini emojis around the dish
                Text("🍚", fontSize = 22.sp, modifier = Modifier.align(Alignment.TopStart).offset(x = 12.dp, y = (20 + floatOffset * 0.5f).dp))
                Text("🧀", fontSize = 18.sp, modifier = Modifier.align(Alignment.TopEnd).offset(x = (-8).dp, y = (32 + floatOffset * 0.3f).dp))
                Text("🥘", fontSize = 20.sp, modifier = Modifier.align(Alignment.BottomStart).offset(x = 16.dp, y = (-24 - floatOffset * 0.4f).dp))
                Text("🍮", fontSize = 18.sp, modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-12).dp, y = (-18 - floatOffset * 0.6f).dp))
            }
        }

        Spacer(Modifier.height(32.dp))

        // Text content
        AnimatedVisibility(visible, enter = fadeIn(tween(600, 200)) + slideInVertically(tween(600, 200)) { 40 }) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text       = "No Orders Yet",
                    style      = MaterialTheme.typography.headlineMedium,
                    color      = Ivory,
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center,
                )
                Text(
                    text      = "Your royal dining journey starts here.\nExplore our menu and place your first order.",
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = IvoryDim.copy(0.55f),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // CTA button
        AnimatedVisibility(visible, enter = fadeIn(tween(500, 400)) + slideInVertically(tween(500, 400)) { 32 }) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                GoldButton(
                    text     = "Explore Menu",
                    onClick  = onOrderNow,
                    modifier = Modifier.fillMaxWidth(),
                )
                GhostButton(
                    text    = "Book a Table Instead",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // Featured dish suggestions
        AnimatedVisibility(visible, enter = fadeIn(tween(500, 600))) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(Modifier.weight(1f).height(1.dp).background(GoldBorder))
                    Text(
                        "Popular right now",
                        style    = MaterialTheme.typography.labelSmall,
                        color    = IvoryDim.copy(0.4f),
                        fontSize = 10.sp,
                    )
                    Box(Modifier.weight(1f).height(1.dp).background(GoldBorder))
                }
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    SuggestionChip("🍚", "Veg Biryani", "₹420", Modifier.weight(1f), onOrderNow)
                    SuggestionChip("🧀", "Paneer Tikka", "₹260", Modifier.weight(1f), onOrderNow)
                    SuggestionChip("🍮", "Shahi Tukda", "₹180", Modifier.weight(1f), onOrderNow)
                }
            }
        }

        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun SuggestionChip(
    emoji:    String,
    name:     String,
    price:    String,
    modifier: Modifier,
    onClick:  () -> Unit,
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.93f else 1f, spring(Spring.DampingRatioMediumBouncy), label = "chip")

    Column(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(Surface1)
            .border(1.dp, GoldBorder, RoundedCornerShape(16.dp))
            .clickable {
                pressed = true
                onClick()
            }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(emoji, fontSize = 26.sp)
        Text(name, style = MaterialTheme.typography.labelSmall, color = IvoryDim.copy(0.8f), textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 10.sp)
        Text(price, style = MaterialTheme.typography.labelSmall, color = Gold, fontWeight = FontWeight.Bold, fontSize = 10.sp)
    }

    // Reset pressed state
    LaunchedEffect(pressed) { if (pressed) { delay(200); pressed = false } }
}

/* ── Shimmer skeleton ── */
@Composable
private fun OrdersShimmer(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier            = modifier.fillMaxSize(),
        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { ShimmerBox(Modifier.fillMaxWidth().height(82.dp), RoundedCornerShape(20.dp)) }
        items(3) { ShimmerBox(Modifier.fillMaxWidth().height(140.dp), RoundedCornerShape(20.dp)) }
    }
}

/* ── Error state ── */
@Composable
private fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier            = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("⚠️", fontSize = 52.sp)
        Spacer(Modifier.height(16.dp))
        Text(message, style = MaterialTheme.typography.bodySmall, color = IvoryDim.copy(0.6f), textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        GoldButton("Retry", onRetry, Modifier.width(130.dp))
    }
}
