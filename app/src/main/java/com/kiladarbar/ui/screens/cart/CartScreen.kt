package com.kiladarbar.ui.screens.cart

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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.kiladarbar.data.remote.dto.CartDto
import com.kiladarbar.data.remote.dto.CartItemDto
import com.kiladarbar.ui.components.*
import com.kiladarbar.ui.models.UiMenuItem
import com.kiladarbar.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun CartScreen(
    onBack:             () -> Unit,
    onCheckout:         () -> Unit,
    onContinueShopping: () -> Unit,
    onLoginRequired:    () -> Unit = {},
    viewModel:          CartViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    // Login required dialog
    if (uiState.showLoginDialog) {
        LoginRequiredDialog(
            onLogin   = { viewModel.dismissLoginDialog(); onLoginRequired() },
            onDismiss = viewModel::dismissLoginDialog,
        )
    }

    Scaffold(
        containerColor = Obsidian,
        topBar = {
            CartTopBar(
                itemCount = uiState.cart?.itemCount ?: 0,
                onBack    = onBack,
                onClear   = { if (!uiState.isGuest) viewModel.clearCart() },
                isGuest   = uiState.isGuest,
            )
        },
        bottomBar = {
            if (!uiState.isLoading && uiState.cart != null && (uiState.cart!!.itemCount) > 0) {
                CartBottomBar(
                    cart      = uiState.cart!!,
                    isGuest   = uiState.isGuest,
                    onCheckout = { viewModel.requestCheckout(onCheckout) },
                )
            }
        },
    ) { padding ->
        when {
            uiState.isLoading -> CartShimmer(Modifier.padding(padding))

            // Only show error to authenticated users — guests always get the empty cart UI
            uiState.error != null && !uiState.isGuest && uiState.cart == null ->
                CartErrorState(uiState.error!!, viewModel::load, Modifier.padding(padding))

            uiState.cart == null || uiState.cart!!.itemCount == 0 ->
                EmptyCartState(
                    isGuest         = uiState.isGuest,
                    suggestedItems  = uiState.suggestedItems,
                    onBrowseMenu    = onContinueShopping,
                    onLoginRequired = { viewModel.showLoginDialog() },
                    onAddSuggested  = viewModel::addSuggestedItem,
                    modifier        = Modifier.padding(padding),
                )

            else ->
                CartContent(
                    cart           = uiState.cart!!,
                    suggestedItems = uiState.suggestedItems,
                    onUpdateItem   = viewModel::updateItem,
                    onAddSuggested = viewModel::addSuggestedItem,
                    modifier       = Modifier.padding(padding),
                )
        }
    }
}

/* ── Top bar ── */
@Composable
private fun CartTopBar(itemCount: Int, onBack: () -> Unit, onClear: () -> Unit, isGuest: Boolean) {
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
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "MY CART",
                style      = MaterialTheme.typography.titleSmall,
                color      = Ivory,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold,
            )
            if (itemCount > 0) {
                Text(
                    "$itemCount item${if (itemCount > 1) "s" else ""}",
                    style  = MaterialTheme.typography.labelSmall,
                    color  = Gold,
                    fontSize = 10.sp,
                )
            }
        }
        if (itemCount > 0 && !isGuest) {
            TextButton(onClick = onClear) {
                Text("Clear", style = MaterialTheme.typography.labelSmall, color = Color(0xFFEF5350))
            }
        } else {
            Spacer(Modifier.width(64.dp))
        }
    }
}

/* ── Cart content (has items) ── */
@Composable
private fun CartContent(
    cart:           CartDto,
    suggestedItems: List<UiMenuItem>,
    onUpdateItem:   (String, Int) -> Unit,
    onAddSuggested: (String) -> Unit,
    modifier:       Modifier = Modifier,
) {
    LazyColumn(
        modifier            = modifier.fillMaxSize().background(Obsidian),
        contentPadding      = PaddingValues(bottom = 160.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // Cart items
        items(cart.items, key = { it.menuItemId }) { item ->
            CartItemRow(item = item, onUpdateQty = { qty -> onUpdateItem(item.menuItemId, qty) })
        }

        item { Spacer(Modifier.height(8.dp)) }

        // Bill summary
        item { BillSummaryCard(cart = cart) }

        item { Spacer(Modifier.height(16.dp)) }

        // You might also like
        if (suggestedItems.isNotEmpty()) {
            item {
                YouMightAlsoLike(items = suggestedItems, onAdd = onAddSuggested)
            }
        }
    }
}

/* ── Cart item row ── */
@Composable
private fun CartItemRow(item: CartItemDto, onUpdateQty: (Int) -> Unit) {
    val haptic = LocalHapticFeedback.current
    var removing by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible     = !removing,
        exit        = shrinkVertically(tween(300)) + fadeOut(tween(200)),
        modifier    = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Surface1)
                .border(1.dp, GoldBorder, RoundedCornerShape(18.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            // Emoji thumbnail
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.radialGradient(listOf(Maroon.copy(0.4f), Surface2))),
                contentAlignment = Alignment.Center,
            ) {
                Text(menuEmoji(item.name), fontSize = 34.sp)
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    item.name,
                    style      = MaterialTheme.typography.titleSmall,
                    color      = Ivory,
                    fontWeight = FontWeight.SemiBold,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                )
                if (!item.specialInstruction.isNullOrBlank()) {
                    Text(
                        "Note: ${item.specialInstruction}",
                        style    = MaterialTheme.typography.labelSmall,
                        color    = IvoryDim.copy(0.45f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 10.sp,
                    )
                }
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    AnimatedContent(
                        targetState = item.totalPrice,
                        transitionSpec = { slideInVertically { it } + fadeIn() togetherWith slideOutVertically { -it } + fadeOut() },
                        label = "price",
                    ) { price ->
                        Text(
                            "₹%.0f".format(price),
                            style      = MaterialTheme.typography.titleMedium,
                            color      = Gold,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    // Stepper
                    Row(
                        modifier          = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(LuxuryGradients.goldHorizontal)
                            .padding(horizontal = 4.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        StepperBtn(Icons.Filled.Remove) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (item.quantity <= 1) removing = true
                            onUpdateQty(item.quantity - 1)
                        }
                        AnimatedContent(targetState = item.quantity, label = "qty") { q ->
                            Text(
                                q.toString(),
                                modifier   = Modifier.widthIn(min = 22.dp),
                                style      = MaterialTheme.typography.labelLarge,
                                color      = Obsidian,
                                fontWeight = FontWeight.Bold,
                                textAlign  = TextAlign.Center,
                            )
                        }
                        StepperBtn(Icons.Filled.Add) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onUpdateQty(item.quantity + 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepperBtn(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(Obsidian.copy(0.2f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = Obsidian, modifier = Modifier.size(14.dp))
    }
}

/* ── Bill summary ── */
@Composable
private fun BillSummaryCard(cart: CartDto) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Surface1)
            .border(1.dp, GoldBorder, RoundedCornerShape(20.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("Bill Summary", style = MaterialTheme.typography.titleMedium, color = Ivory, fontWeight = FontWeight.Bold)
        GoldDivider()
        BillRow("Subtotal",      "₹%.0f".format(cart.subtotal))
        BillRow("Discount",      if (cart.discountAmount > 0) "-₹%.0f".format(cart.discountAmount) else "—",
            valueColor = if (cart.discountAmount > 0) Color(0xFF4CAF50) else IvoryDim.copy(0.4f))
        BillRow("GST (5%)",      "₹%.0f".format(cart.gstAmount))
        BillRow("Delivery",
            if (cart.deliveryCharge == 0.0) "FREE" else "₹%.0f".format(cart.deliveryCharge),
            valueColor = if (cart.deliveryCharge == 0.0) Color(0xFF4CAF50) else IvoryDim.copy(0.7f))

        if (cart.appliedCoupon != null) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Text("Coupon Applied", style = MaterialTheme.typography.bodySmall, color = IvoryDim.copy(0.6f))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF4CAF50).copy(0.15f))
                        .border(0.5.dp, Color(0xFF4CAF50).copy(0.4f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(Icons.Filled.LocalOffer, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(11.dp))
                    Text(cart.appliedCoupon, style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
            }
        }

        if (cart.deliveryCharge > 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF4CAF50).copy(0.08f))
                    .border(0.5.dp, Color(0xFF4CAF50).copy(0.25f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(
                    "Add ₹%.0f more for free delivery".format(cart.freeDeliveryAbove - cart.subtotal),
                    style    = MaterialTheme.typography.labelSmall,
                    color    = Color(0xFF4CAF50),
                    fontSize = 10.sp,
                )
            }
        }

        GoldDivider()
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text("TOTAL", style = MaterialTheme.typography.labelLarge, color = Ivory, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            AnimatedContent(targetState = cart.totalAmount, transitionSpec = { slideInVertically { it } + fadeIn() togetherWith slideOutVertically { -it } + fadeOut() }, label = "total") { t ->
                Text("₹%.0f".format(t), style = MaterialTheme.typography.headlineSmall, color = Gold, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun BillRow(label: String, value: String, valueColor: Color = IvoryDim.copy(0.7f)) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = IvoryDim.copy(0.5f))
        Text(value, style = MaterialTheme.typography.bodySmall, color = valueColor, fontWeight = FontWeight.SemiBold)
    }
}

/* ── You might also like ── */
@Composable
private fun YouMightAlsoLike(items: List<UiMenuItem>, onAdd: (String) -> Unit) {
    Column(
        modifier            = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LuxurySectionHeader(
            title    = "You Might Also Like",
            subtitle = "Popular picks from our kitchen",
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        LazyRow(
            contentPadding        = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(items, key = { it.id }) { item ->
                SuggestedItemCard(item = item, onAdd = { onAdd(item.id) })
            }
        }
    }
}

@Composable
private fun SuggestedItemCard(item: UiMenuItem, onAdd: () -> Unit) {
    var added by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (added) 0.95f else 1f, spring(Spring.DampingRatioMediumBouncy), label = "addScale")

    LaunchedEffect(added) {
        if (added) { delay(1000); added = false }
    }

    Column(
        modifier = Modifier
            .width(150.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Surface1)
            .border(1.dp, GoldBorder, RoundedCornerShape(18.dp))
            .scale(scale),
    ) {
        // Emoji visual
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Brush.radialGradient(listOf(Maroon.copy(0.4f), Surface2))),
            contentAlignment = Alignment.Center,
        ) {
            Text(menuEmoji(item.name), fontSize = 44.sp)
            Box(modifier = Modifier.align(Alignment.TopStart).padding(6.dp)) {
                RoyalBadge(item.tag, isGold = true)
            }
        }

        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(item.name, style = MaterialTheme.typography.labelMedium, color = Ivory, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text(
                    "₹${item.discountPrice?.toInt() ?: item.price.toInt()}",
                    style      = MaterialTheme.typography.labelMedium,
                    color      = Gold,
                    fontWeight = FontWeight.Bold,
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            if (added) Brush.linearGradient(listOf(Color(0xFF4CAF50), Color(0xFF388E3C)))
                            else LuxuryGradients.goldHorizontal
                        )
                        .clickable { if (!added) { added = true; onAdd() } },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        if (added) Icons.Filled.Check else Icons.Filled.Add,
                        null,
                        tint     = Obsidian,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
    }
}

/* ── Sticky bottom checkout bar ── */
@Composable
private fun CartBottomBar(cart: CartDto, isGuest: Boolean, onCheckout: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(Color.Transparent, Obsidian.copy(0.95f), Obsidian))
            )
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text("Total", style = MaterialTheme.typography.labelSmall, color = IvoryDim.copy(0.5f))
                Text("₹%.0f".format(cart.totalAmount), style = MaterialTheme.typography.headlineSmall, color = Gold, fontWeight = FontWeight.Bold)
                if (cart.discountAmount > 0) {
                    Text("Saving ₹%.0f".format(cart.discountAmount), style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CAF50), fontSize = 10.sp)
                }
            }

            if (isGuest) {
                // Guest: show "Login to Order" CTA
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(LuxuryGradients.goldHorizontal)
                        .clickable { onCheckout() }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Filled.Lock, null, tint = Obsidian, modifier = Modifier.size(14.dp))
                        Text("Login to Order", style = MaterialTheme.typography.labelLarge, color = Obsidian, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    }
                    Text("Sign in to place your order", style = MaterialTheme.typography.labelSmall, color = Obsidian.copy(0.65f), fontSize = 9.sp)
                }
            } else {
                GoldButton(
                    text     = "Proceed to Checkout",
                    onClick  = onCheckout,
                    modifier = Modifier.widthIn(min = 200.dp),
                )
            }
        }
    }
}

/* ── Login required dialog (replaces the old bottom sheet) ── */
@Composable
private fun LoginRequiredDialog(onLogin: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = Surface1,
        shape            = RoundedCornerShape(24.dp),
        icon = {
            Text("👑", fontSize = 40.sp)
        },
        title = {
            Text(
                "Login Required",
                style      = MaterialTheme.typography.titleLarge,
                color      = Ivory,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center,
                modifier   = Modifier.fillMaxWidth(),
            )
        },
        text = {
            Text(
                "Sign in to place your order. Your cart items will be saved and waiting for you.",
                style     = MaterialTheme.typography.bodySmall,
                color     = IvoryDim.copy(0.65f),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier  = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(LuxuryGradients.goldHorizontal)
                    .clickable { onLogin() }
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Login / Register",
                    style         = MaterialTheme.typography.labelLarge,
                    color         = Obsidian,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                )
            }
        },
        dismissButton = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, GoldBorder, RoundedCornerShape(12.dp))
                    .clickable { onDismiss() }
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Continue Browsing",
                    style      = MaterialTheme.typography.labelLarge,
                    color      = Gold,
                    fontWeight = FontWeight.Medium,
                )
            }
        },
    )
}

/* ── Empty cart state ── */
@Composable
private fun EmptyCartState(
    isGuest:         Boolean,
    suggestedItems:  List<UiMenuItem>,
    onBrowseMenu:    () -> Unit,
    onLoginRequired: () -> Unit,
    onAddSuggested:  (String) -> Unit,
    modifier:        Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cartFloat")
    val floatY by infiniteTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = -12f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label         = "cartY",
    )

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); visible = true }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(36.dp))

        // Cart icon animation
        AnimatedVisibility(visible, enter = fadeIn(tween(600)) + scaleIn(spring(Spring.DampingRatioLowBouncy), 0.5f)) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(180.dp)) {
                // Glow ring
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(Maroon.copy(0.25f), Color.Transparent)))
                )
                // Cart circle
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Surface1)
                        .border(1.dp, GoldBorder, CircleShape)
                        .offset(y = floatY.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("🛒", fontSize = 48.sp)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Messaging — differs for guest vs logged-in
        AnimatedVisibility(visible, enter = fadeIn(tween(500, 200)) + slideInVertically(tween(500, 200)) { 32 }) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text       = if (isGuest) "Your Cart Awaits" else "Your Cart is Empty",
                    style      = MaterialTheme.typography.headlineMedium,
                    color      = Ivory,
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center,
                )
                Text(
                    text = if (isGuest)
                        "Add your favourite royal dishes. When you're ready to order, simply sign in — your cart will be waiting."
                    else
                        "Looks like you haven't added anything yet. Let's fix that.",
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = IvoryDim.copy(0.55f),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        // CTAs
        AnimatedVisibility(visible, enter = fadeIn(tween(500, 350))) {
            Column(
                modifier            = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                GoldButton("Explore the Menu", onBrowseMenu, Modifier.fillMaxWidth())
                if (isGuest) {
                    GhostButton("Sign In to Continue", onLoginRequired, Modifier.fillMaxWidth())
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        // Suggested items
        if (suggestedItems.isNotEmpty()) {
            AnimatedVisibility(visible, enter = fadeIn(tween(500, 500))) {
                Column(
                    modifier            = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(Modifier.weight(1f).height(1.dp).background(GoldBorder))
                        Text(
                            "You might like",
                            style    = MaterialTheme.typography.labelSmall,
                            color    = IvoryDim.copy(0.4f),
                            fontSize = 10.sp,
                        )
                        Box(Modifier.weight(1f).height(1.dp).background(GoldBorder))
                    }

                    LazyRow(
                        contentPadding        = PaddingValues(horizontal = 0.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(suggestedItems, key = { it.id }) { item ->
                            SuggestedItemCard(item = item, onAdd = { onAddSuggested(item.id) })
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(80.dp))
    }
}

/* ── Loading shimmer ── */
@Composable
private fun CartShimmer(modifier: Modifier = Modifier) {
    Column(
        modifier            = modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        repeat(3) { ShimmerBox(Modifier.fillMaxWidth().height(96.dp), RoundedCornerShape(18.dp)) }
        Spacer(Modifier.height(4.dp))
        ShimmerBox(Modifier.fillMaxWidth().height(180.dp), RoundedCornerShape(20.dp))
    }
}

/* ── Error state ── */
@Composable
private fun CartErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
        Text("⚠️", fontSize = 52.sp)
        Spacer(Modifier.height(16.dp))
        Text(message, style = MaterialTheme.typography.bodySmall, color = IvoryDim.copy(0.6f), textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        GoldButton("Retry", onRetry, Modifier.width(130.dp))
    }
}

private fun menuEmoji(name: String) = when {
    name.contains("biryani", ignoreCase = true) -> "🍚"
    name.contains("paneer",  ignoreCase = true) -> "🧀"
    name.contains("dal",     ignoreCase = true) -> "🥘"
    name.contains("lassi",   ignoreCase = true) -> "🍹"
    name.contains("roti",    ignoreCase = true) || name.contains("naan", ignoreCase = true) -> "🫓"
    name.contains("tukda",   ignoreCase = true) || name.contains("gulab", ignoreCase = true) -> "🍮"
    else -> "🍽️"
}
