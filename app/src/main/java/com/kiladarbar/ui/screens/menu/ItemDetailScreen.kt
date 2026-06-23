package com.kiladarbar.ui.screens.menu

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.kiladarbar.ui.components.*
import com.kiladarbar.ui.models.UiMenuItem
import com.kiladarbar.ui.theme.*
import kotlinx.coroutines.delay

private val ingredients = listOf(
    "🌾 Aged Basmati", "🌿 Saffron", "🧅 Fried Onions",
    "🥛 Pure Ghee", "🌶 Whole Spices", "🍃 Mint",
    "🥕 Fresh Vegetables", "🧀 Paneer",
)

private val reviews = listOf(
    Triple("Arjun M.",   5, "Transcendent. Best biryani I've had outside Hyderabad."),
    Triple("Fatima S.",  5, "Melt-in-mouth mutton, perfectly spiced rice. 10/10."),
    Triple("Rohan V.",   4, "Excellent! Portion is generous. Will order again."),
)

@Composable
fun ItemDetailScreen(
    itemId:     String,
    onBack:     () -> Unit,
    onCartClick: () -> Unit,
    viewModel:  ItemDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    val heroScale    = remember { Animatable(0.92f) }
    val heroAlpha    = remember { Animatable(0f) }
    val contentAlpha = remember { Animatable(0f) }
    val contentY     = remember { Animatable(48f) }

    LaunchedEffect(Unit) {
        heroAlpha.animateTo(1f, tween(400))
        heroScale.animateTo(1f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMediumLow))
        delay(200)
        contentAlpha.animateTo(1f, tween(500))
        contentY.animateTo(0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow))
    }

    Box(modifier = Modifier.fillMaxSize().background(Obsidian)) {

        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color    = Gold,
                )
            }
            uiState.error != null && uiState.item == null -> {
                Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⚠️", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(uiState.error!!, style = MaterialTheme.typography.bodySmall, color = IvoryDim.copy(0.6f))
                }
            }
            uiState.item != null -> {
                ItemDetailContent(
                    item         = uiState.item!!,
                    qty          = uiState.qty,
                    heroScale    = heroScale.value,
                    heroAlpha    = heroAlpha.value,
                    contentAlpha = contentAlpha.value,
                    contentY     = contentY.value,
                    onBack       = onBack,
                    onAddToCart  = viewModel::addToCart,
                    onIncrement  = viewModel::increment,
                    onDecrement  = viewModel::decrement,
                )
            }
        }
    }
}

@Composable
private fun ItemDetailContent(
    item:         UiMenuItem,
    qty:          Int,
    heroScale:    Float,
    heroAlpha:    Float,
    contentAlpha: Float,
    contentY:     Float,
    onBack:       () -> Unit,
    onAddToCart:  () -> Unit,
    onIncrement:  () -> Unit,
    onDecrement:  () -> Unit,
) {
    val displayPrice = (item.discountPrice ?: item.price) * qty.coerceAtLeast(1)

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {

        // Hero
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .scale(heroScale)
                    .alpha(heroAlpha)
                    .background(
                        Brush.linearGradient(
                            colorStops = arrayOf(0f to MaroonDark, 0.6f to Maroon, 1f to Obsidian)
                        )
                    ),
            ) {
                Box(
                    modifier = Modifier
                        .size(400.dp)
                        .align(Alignment.Center)
                        .background(Brush.radialGradient(listOf(Gold.copy(0.05f), Color.Transparent)))
                )
                Text(menuEmoji(item.name), fontSize = 160.sp, modifier = Modifier.align(Alignment.Center))
                Box(
                    modifier = Modifier
                        .fillMaxWidth().height(120.dp).align(Alignment.BottomCenter)
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Obsidian)))
                )
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.clip(CircleShape).background(Obsidian.copy(0.6f))) {
                        Icon(Icons.Filled.ArrowBack, null, tint = Ivory)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = {}, modifier = Modifier.clip(CircleShape).background(Obsidian.copy(0.6f))) {
                            Icon(Icons.Filled.FavoriteBorder, null, tint = Ivory)
                        }
                        IconButton(onClick = {}, modifier = Modifier.clip(CircleShape).background(Obsidian.copy(0.6f))) {
                            Icon(Icons.Filled.Share, null, tint = Ivory)
                        }
                    }
                }
            }
        }

        // Content
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(contentAlpha)
                    .offset(y = contentY.dp)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Spacer(Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FoodTypeIndicator(isVeg = item.isVeg, modifier = Modifier.size(18.dp))
                    RoyalBadge(item.tag, isGold = item.isBestSeller)
                }

                Text(
                    text      = item.name,
                    style     = MaterialTheme.typography.headlineMedium,
                    color     = Ivory,
                    fontWeight = FontWeight.Bold,
                )

                Text(
                    text       = item.description,
                    style      = MaterialTheme.typography.bodyMedium,
                    color      = IvoryDim.copy(alpha = 0.7f),
                    lineHeight = 22.sp,
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (item.rating > 0f) StatChip("⭐", "%.1f".format(item.rating), "Rating")
                    if (item.preparationTime != null) StatChip("⏱️", "${item.preparationTime} min", "Prep Time")
                    if (item.calories != null) StatChip("🔥", "${item.calories} cal", "Calories")
                }

                GoldDivider()

                Text("Royal Ingredients", style = MaterialTheme.typography.titleMedium, color = Ivory, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ingredients) { ing ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Surface1)
                                .border(0.5.dp, GoldBorder, RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        ) { Text(ing, style = MaterialTheme.typography.labelSmall, color = IvoryDim.copy(0.8f)) }
                    }
                }

                GoldDivider()

                Text("Guest Reviews", style = MaterialTheme.typography.titleMedium, color = Ivory, fontWeight = FontWeight.SemiBold)
                reviews.forEach { (name, rating, text) ->
                    ReviewCard(name = name, rating = rating, text = text)
                }

                Spacer(Modifier.height(110.dp))
            }
        }
        } // end LazyColumn

    // Floating CTA
    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color.Transparent, Obsidian.copy(0.95f), Obsidian)))
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Column {
                Text("Total Price", style = MaterialTheme.typography.labelSmall, color = IvoryDim.copy(0.5f))
                Text("₹${"%.0f".format(displayPrice)}", style = MaterialTheme.typography.headlineSmall, color = Gold, fontWeight = FontWeight.Bold)
            }

            AnimatedContent(
                targetState = qty > 0,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                label = "cartCta",
            ) { hasQty ->
                if (!hasQty) {
                    GoldButton(text = "Add to Cart", onClick = onAddToCart, modifier = Modifier.width(180.dp))
                } else {
                    QuantityStepper(qty, onIncrement, onDecrement, modifier = Modifier.width(180.dp).height(52.dp))
                }
            }
        }
    } // end CTA Box
    } // end outer Box
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

@Composable
private fun StatChip(icon: String, value: String, label: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Surface1)
            .border(0.5.dp, GoldBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(icon, fontSize = 18.sp)
        Text(value, style = MaterialTheme.typography.labelMedium, color = Gold, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = IvoryDim.copy(0.5f), fontSize = 9.sp)
    }
}

@Composable
private fun ReviewCard(name: String, rating: Int, text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Surface1)
            .border(0.5.dp, GoldBorder, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    Modifier.size(32.dp).clip(CircleShape).background(LuxuryGradients.maroonVertical),
                    Alignment.Center,
                ) { Text(name.first().toString(), color = Ivory, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                Text(name, style = MaterialTheme.typography.labelMedium, color = Ivory, fontWeight = FontWeight.SemiBold)
            }
            Row { repeat(rating) { Icon(Icons.Filled.Star, null, tint = Gold, modifier = Modifier.size(12.dp)) } }
        }
        Text(text, style = MaterialTheme.typography.bodySmall, color = IvoryDim.copy(0.7f), lineHeight = 18.sp)
    }
}
