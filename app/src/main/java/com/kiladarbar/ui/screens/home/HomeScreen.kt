package com.kiladarbar.ui.screens.home

import androidx.compose.animation.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kiladarbar.ui.components.FoodTypeIndicator
import com.kiladarbar.ui.components.RoyalScaffold
import com.kiladarbar.ui.components.SectionHeader
import com.kiladarbar.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onMenuClick: () -> Unit,
    onCategoryClick: (Int) -> Unit,
    onItemClick: (String) -> Unit,
    onCartClick: () -> Unit,
    onOrdersClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    RoyalScaffold(
        topBar = {
            HomeTopBar(
                userName = uiState.userName,
                cartCount = uiState.cartCount,
                onCartClick = onCartClick,
                onProfileClick = onProfileClick,
            )
        },
        bottomBar = {
            HomeBottomNav(
                onHomeClick = {},
                onMenuClick = onMenuClick,
                onOrdersClick = onOrdersClick,
                onProfileClick = onProfileClick,
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            // Hero banner
            item {
                HeroBanner(
                    onOrderNow = onMenuClick,
                    onBookTable = { /* navigate to reservations */ }
                )
            }

            // Quick actions
            item {
                QuickActions(
                    onDelivery   = onMenuClick,
                    onPickup     = onMenuClick,
                    onDineIn     = { /* table reservation */ },
                    onCatering   = { /* catering */ },
                )
            }

            // Offers strip
            if (uiState.activeOffers.isNotEmpty()) {
                item {
                    OffersStrip(offers = uiState.activeOffers)
                }
            }

            // Categories
            item {
                SectionHeader(
                    title = "What's on the Menu",
                    subtitle = "Explore our royal categories",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
            item {
                CategoriesRow(
                    categories = uiState.categories,
                    onCategoryClick = onCategoryClick,
                )
            }

            // Best sellers
            item {
                SectionHeader(
                    title = "Best Sellers",
                    subtitle = "Our guests' favourites",
                    actionLabel = "See All",
                    onAction = onMenuClick,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
            items(
                items = uiState.bestSellers,
                key = { it.id }
            ) { item ->
                MenuItemRow(
                    item = item,
                    onItemClick = { onItemClick(item.id) },
                    onAddToCart = { viewModel.addToCart(item) },
                    quantity = uiState.cartQuantities[item.id] ?: 0,
                    onQuantityChange = { qty -> viewModel.updateCartQuantity(item, qty) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }

            // Recommended
            item {
                SectionHeader(
                    title = "Chef's Recommendations",
                    subtitle = "Handpicked by our master chefs",
                    actionLabel = "View All",
                    onAction = onMenuClick,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
            item {
                RecommendedRow(
                    items = uiState.recommended,
                    onItemClick = onItemClick,
                )
            }

            // Loyalty banner
            if (uiState.isLoggedIn) {
                item {
                    LoyaltyBanner(
                        points = uiState.loyaltyPoints,
                        tier = uiState.loyaltyTier,
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun HomeTopBar(
    userName: String?,
    cartCount: Int,
    onCartClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    Surface(
        color = RoyalMaroon,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = if (userName != null) "Welcome back," else "Welcome to",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                )
                Text(
                    text = userName ?: "Kila Darbar",
                    style = MaterialTheme.typography.titleLarge,
                    color = RoyalGold,
                    fontWeight = FontWeight.Bold,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Search
                IconButton(onClick = { /* search */ }) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                }

                // Cart with badge
                BadgedBox(
                    badge = {
                        if (cartCount > 0) {
                            Badge(containerColor = RoyalGold) {
                                Text(
                                    text = if (cartCount > 9) "9+" else "$cartCount",
                                    color = RoyalDark,
                                )
                            }
                        }
                    }
                ) {
                    IconButton(onClick = onCartClick) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = Color.White)
                    }
                }

                // Profile avatar
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(RoyalGold)
                        .clickable { onProfileClick() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = userName?.firstOrNull()?.uppercaseChar()?.toString() ?: "G",
                        color = RoyalDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroBanner(
    onOrderNow: () -> Unit,
    onBookTable: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                Brush.linearGradient(
                    colors = listOf(RoyalMaroon, RoyalMaroonDark)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(24.dp),
        ) {
            Text(
                text = "Royal Mughal Feast",
                style = MaterialTheme.typography.headlineMedium,
                color = RoyalGold,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Authentic flavors, delivered to you",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onOrderNow,
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGold),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("Order Now", color = RoyalDark, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onBookTable,
                    border = BorderStroke(1.dp, Color.White),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("Book Table", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun QuickActions(
    onDelivery: () -> Unit,
    onPickup: () -> Unit,
    onDineIn: () -> Unit,
    onCatering: () -> Unit,
) {
    val actions = listOf(
        Triple("Delivery",  Icons.Default.LocalShipping, onDelivery),
        Triple("Pickup",    Icons.Default.Store,         onPickup),
        Triple("Dine In",   Icons.Default.TableBar,      onDineIn),
        Triple("Catering",  Icons.Default.Event,         onCatering),
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        actions.forEach { (label, icon, onClick) ->
            QuickActionButton(
                label = label,
                icon = icon,
                onClick = onClick,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = RoyalMaroon,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun LoyaltyBanner(
    points: Int,
    tier: String,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = RoyalGoldDark.copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.4f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = "Your Loyalty Points",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
                Text(
                    text = "$points pts",
                    style = MaterialTheme.typography.headlineSmall,
                    color = RoyalGold,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "🏆 $tier Member",
                    style = MaterialTheme.typography.bodySmall,
                    color = RoyalMaroon,
                )
            }
            Icon(
                imageVector = Icons.Default.Stars,
                contentDescription = "Loyalty",
                tint = RoyalGold,
                modifier = Modifier.size(40.dp),
            )
        }
    }
}

// Placeholder composables referenced above
@Composable fun OffersStrip(offers: List<Any>) {}
@Composable fun CategoriesRow(categories: List<Any>, onCategoryClick: (Int) -> Unit) {}
@Composable fun MenuItemRow(item: Any, onItemClick: () -> Unit, onAddToCart: () -> Unit, quantity: Int, onQuantityChange: (Int) -> Unit, modifier: Modifier = Modifier) {}
@Composable fun RecommendedRow(items: List<Any>, onItemClick: (String) -> Unit) {}
@Composable fun HomeBottomNav(onHomeClick: () -> Unit, onMenuClick: () -> Unit, onOrdersClick: () -> Unit, onProfileClick: () -> Unit) {}
