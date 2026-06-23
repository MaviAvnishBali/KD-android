package com.kiladarbar.ui.screens.menu

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.kiladarbar.ui.components.*
import com.kiladarbar.ui.models.UiCategory
import com.kiladarbar.ui.models.UiMenuItem
import com.kiladarbar.ui.theme.*

@Composable
fun MenuScreen(
    initialCategoryId: Int? = null,
    onItemClick:       (String) -> Unit,
    onCartClick:       () -> Unit,
    onBack:            () -> Unit,
    viewModel:         MenuViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    // Apply initial category filter once
    LaunchedEffect(initialCategoryId) {
        if (initialCategoryId != null && initialCategoryId != 0) {
            viewModel.selectCategory(initialCategoryId)
        }
    }

    Scaffold(
        containerColor = Obsidian,
        topBar = {
            MenuTopBar(
                searchQuery    = uiState.searchQuery,
                onSearchChange = viewModel::onSearchChange,
                onBack         = onBack,
                onCartClick    = onCartClick,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).background(Obsidian),
        ) {
            // Category tabs
            CategoryTabBar(
                categories       = uiState.categories,
                selectedId       = uiState.selectedCategoryId,
                isLoading        = uiState.isLoadingCategories,
                onCategorySelect = viewModel::selectCategory,
            )

            // Items list
            when {
                uiState.isLoadingItems -> {
                    LazyColumn(
                        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(6) { MenuItemShimmer() }
                    }
                }
                uiState.error != null && uiState.items.isEmpty() -> {
                    ErrorState(message = uiState.error!!, onRetry = { viewModel.loadItems() })
                }
                uiState.items.isEmpty() -> {
                    EmptyMenuState()
                }
                else -> {
                    LazyColumn(
                        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(uiState.items, key = { it.id }) { item ->
                            val qty = uiState.cartQuantities[item.id] ?: 0
                            MenuItemRow(
                                item        = item,
                                qty         = qty,
                                onItemClick = { onItemClick(item.id) },
                                onAdd       = { viewModel.addToCart(item) },
                                onIncrement = { viewModel.addToCart(item) },
                                onDecrement = { viewModel.decrementCart(item) },
                            )
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

/* ── Top bar ── */
@Composable
private fun MenuTopBar(
    searchQuery:    String,
    onSearchChange: (String) -> Unit,
    onBack:         () -> Unit,
    onCartClick:    () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().background(Obsidian).statusBarsPadding()) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null, tint = Ivory) }
            Text("THE MENU", style = MaterialTheme.typography.titleMedium, color = Ivory, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1B4332).copy(0.4f))
                        .border(1.dp, Color(0xFF4CAF50).copy(0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                ) { Text("🌿 Pure Veg", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CAF50)) }
                IconButton(onClick = onCartClick) { Icon(Icons.Filled.ShoppingCart, null, tint = Ivory) }
            }
        }
        // Search bar
        Box(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(14.dp)).background(Surface1)
                .border(1.dp, GoldBorder, RoundedCornerShape(14.dp)),
        ) {
            Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Filled.Search, null, tint = IvoryDim.copy(0.5f), modifier = Modifier.size(18.dp))
                BasicTextField(
                    value         = searchQuery,
                    onValueChange = onSearchChange,
                    modifier      = Modifier.weight(1f),
                    textStyle     = MaterialTheme.typography.bodyMedium.copy(color = Ivory),
                    singleLine    = true,
                    decorationBox = { inner ->
                        Box {
                            if (searchQuery.isEmpty()) Text("Search dishes...", style = MaterialTheme.typography.bodyMedium, color = IvoryDim.copy(0.4f))
                            inner()
                        }
                    },
                )
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }, modifier = Modifier.size(18.dp)) {
                        Icon(Icons.Filled.Clear, null, tint = IvoryDim.copy(0.5f), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

/* ── Category tabs ── */
@Composable
private fun CategoryTabBar(
    categories:       List<UiCategory>,
    selectedId:       Int,
    isLoading:        Boolean,
    onCategorySelect: (Int) -> Unit,
) {
    if (isLoading) {
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(5) { ShimmerBox(modifier = Modifier.size(80.dp, 36.dp), shape = RoundedCornerShape(20.dp)) }
        }
        return
    }

    // Prepend "All" tab
    val allTab = UiCategory(0, "All", "🍽️", null, 0)
    val tabs   = listOf(allTab) + categories

    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(tabs) { cat ->
            val selected = selectedId == cat.id
            val bgAlpha by animateFloatAsState(if (selected) 0.2f else 0f, label = "tabBg")
            val scale   by animateFloatAsState(if (selected) 1.05f else 1f, spring(Spring.DampingRatioMediumBouncy), label = "tabScale")
            Row(
                modifier = Modifier.scale(scale)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Gold.copy(bgAlpha))
                    .border(1.dp, if (selected) Gold.copy(0.6f) else GoldBorder, RoundedCornerShape(20.dp))
                    .clickable { onCategorySelect(cat.id) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(cat.emoji, fontSize = 14.sp)
                Text(
                    text       = cat.name,
                    style      = MaterialTheme.typography.labelMedium,
                    color      = if (selected) Gold else IvoryDim.copy(0.7f),
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}

/* ── Menu item row ── */
@Composable
private fun MenuItemRow(
    item:        UiMenuItem,
    qty:         Int,
    onItemClick: () -> Unit,
    onAdd:       () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
) {
    AnimatedVisibility(visible = true, enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 2 }) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Surface1)
                .border(1.dp, GoldBorder, RoundedCornerShape(20.dp))
                .clickable { onItemClick() }
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(88.dp).clip(RoundedCornerShape(16.dp))
                    .background(Brush.radialGradient(listOf(Maroon.copy(0.4f), Surface2))),
                contentAlignment = Alignment.Center,
            ) {
                Text(menuEmoji(item.name), fontSize = 44.sp)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FoodTypeIndicator(isVeg = item.isVeg, modifier = Modifier.size(14.dp))
                    RoyalBadge(item.tag, isGold = item.tag == "Bestseller")
                }
                Text(item.name, style = MaterialTheme.typography.titleSmall, color = Ivory, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(item.description, style = MaterialTheme.typography.bodySmall, color = IvoryDim.copy(0.5f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (item.rating > 0f) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.Star, null, tint = Gold, modifier = Modifier.size(12.dp))
                        Text("%.1f".format(item.rating), style = MaterialTheme.typography.labelSmall, color = Gold, fontWeight = FontWeight.SemiBold)
                        if (item.preparationTime != null) {
                            Text("· ${item.preparationTime} min", style = MaterialTheme.typography.labelSmall, color = IvoryDim.copy(0.4f))
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Column {
                        if (item.discountPrice != null && item.discountPrice < item.price) {
                            Text("₹${item.price}", style = MaterialTheme.typography.labelSmall, color = IvoryDim.copy(0.4f), modifier = Modifier.run { this })
                        }
                        Text("₹${item.discountPrice ?: item.price}", style = MaterialTheme.typography.titleMedium, color = Gold, fontWeight = FontWeight.Bold)
                    }
                    AnimatedAddButton(
                        quantity    = qty,
                        onAdd       = onAdd,
                        onIncrement = onIncrement,
                        onDecrement = onDecrement,
                    )
                }
            }
        }
    }
}

/* ── States ── */
@Composable
private fun EmptyMenuState() {
    Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
        Text("🍽️", fontSize = 60.sp)
        Spacer(Modifier.height(12.dp))
        Text("Nothing here yet", style = MaterialTheme.typography.titleMedium, color = IvoryDim.copy(0.6f))
        Text("Try a different category", style = MaterialTheme.typography.bodySmall, color = IvoryDim.copy(0.4f))
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
        Text("⚠️", fontSize = 48.sp)
        Spacer(Modifier.height(12.dp))
        Text(message, style = MaterialTheme.typography.bodySmall, color = IvoryDim.copy(0.6f), maxLines = 2)
        Spacer(Modifier.height(16.dp))
        GoldButton("Retry", onRetry, Modifier.width(120.dp))
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
