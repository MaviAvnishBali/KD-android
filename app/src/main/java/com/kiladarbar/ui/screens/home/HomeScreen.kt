package com.kiladarbar.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.kiladarbar.ui.components.*
import com.kiladarbar.ui.models.UiBanner
import com.kiladarbar.ui.models.UiCategory
import com.kiladarbar.ui.models.UiMenuItem
import com.kiladarbar.ui.models.UiOffer
import com.kiladarbar.ui.theme.*
import kotlinx.coroutines.delay

private data class QuickAction(val label: String, val icon: ImageVector, val bg: Color)
private val QUICK_ACTIONS = listOf(
    QuickAction("Delivery",  Icons.Filled.DeliveryDining, Maroon),
    QuickAction("Dine In",   Icons.Filled.TableBar,       Color(0xFF1B4332)),
    QuickAction("Takeaway",  Icons.Filled.Storefront,     Color(0xFF1A237E)),
    QuickAction("Catering",  Icons.Filled.Celebration,    Color(0xFF4A148C)),
)

@Composable
fun HomeScreen(
    onMenuClick:     () -> Unit,
    onCategoryClick: (Int) -> Unit,
    onItemClick:     (String) -> Unit,
    onCartClick:     () -> Unit,
    onOrdersClick:   () -> Unit,
    onProfileClick:  () -> Unit,
    onPartyHallClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState  by viewModel.uiState.collectAsState()
    val userName by viewModel.userName.collectAsState()
    var selectedTab by remember { mutableStateOf(NavTab.Home) }

    Scaffold(
        containerColor = Obsidian,
        bottomBar = {
            LuxuryBottomNav(
                selectedTab   = selectedTab,
                cartCount     = uiState.cartCount,
                onTabSelected = { tab ->
                    selectedTab = tab
                    when (tab) {
                        NavTab.Menu    -> onMenuClick()
                        NavTab.Orders  -> onOrdersClick()
                        NavTab.Profile -> onProfileClick()
                        else           -> {}
                    }
                },
                onCartClick = onCartClick,
            )
        },
    ) { padding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize().background(Obsidian),
            contentPadding = PaddingValues(bottom = padding.calculateBottomPadding() + 16.dp),
        ) {
            item { HomeTopBar(userName = userName, onProfileClick = onProfileClick) }

            // Hero banner — shimmer while loading, real slides once ready
            item {
                if (uiState.isLoading && uiState.banners.isEmpty()) {
                    ShimmerBox(
                        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().height(240.dp),
                        shape    = RoundedCornerShape(24.dp),
                    )
                } else {
                    HeroBannerPager(
                        banners    = uiState.banners,
                        onMenuClick = onMenuClick,
                    )
                }
            }

            // Quick actions
            item { QuickActionsRow(onMenuClick = onMenuClick) }

            // Party Hall CTA
            item { PartyHallCta(onClick = onPartyHallClick) }

            // Offers marquee — shimmer while loading
            item {
                if (uiState.isLoading && uiState.offers.isEmpty()) {
                    ShimmerBox(
                        modifier = Modifier.fillMaxWidth().height(110.dp),
                        shape    = RoundedCornerShape(0.dp),
                    )
                } else if (uiState.offers.isNotEmpty()) {
                    OffersMarquee(offers = uiState.offers)
                }
            }

            // Categories
            item {
                LuxurySectionHeader(
                    title       = "Explore the Menu",
                    subtitle    = "What royal flavour calls to you?",
                    actionLabel = "See all",
                    onAction    = onMenuClick,
                    modifier    = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
                )
            }
            item {
                if (uiState.isLoading && uiState.categories.isEmpty()) {
                    CategoryChipsShimmer()
                } else {
                    CategoryChips(categories = uiState.categories, onCategoryClick = onCategoryClick)
                }
            }

            // Today's special
            if (uiState.recommended.isNotEmpty()) {
                item { TodaysSpecial(item = uiState.recommended.first(), onItemClick = onItemClick) }
            }

            // Best sellers
            item {
                LuxurySectionHeader(
                    title       = "Best Sellers",
                    subtitle    = "Our guests' all-time favourites",
                    actionLabel = "View All",
                    onAction    = onMenuClick,
                    modifier    = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
                )
            }
            item {
                when {
                    uiState.isLoading && uiState.bestSellers.isEmpty() -> BestSellersShimmer()
                    uiState.error != null && uiState.bestSellers.isEmpty() ->
                        ErrorRetry(uiState.error!!, viewModel::loadHome)
                    else -> BestSellersGrid(
                        items          = uiState.bestSellers,
                        onItemClick    = onItemClick,
                        cartQuantities = uiState.cartQuantities,
                        onAdd          = viewModel::addToCart,
                        onIncrement    = { viewModel.updateCartQuantity(it.id, (uiState.cartQuantities[it.id] ?: 0) + 1) },
                        onDecrement    = { viewModel.updateCartQuantity(it.id, (uiState.cartQuantities[it.id] ?: 1) - 1) },
                    )
                }
            }

            if (uiState.isLoggedIn) {
                item { LoyaltySection(points = uiState.loyaltyPoints, tier = uiState.loyaltyTier) }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

/* ── Top bar ── */
@Composable
private fun HomeTopBar(userName: String?, onProfileClick: () -> Unit) {
    val greeting = remember {
        val h = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        when { h < 12 -> "Good Morning"; h < 17 -> "Good Afternoon"; else -> "Good Evening" }
    }
    Row(
        modifier              = Modifier.fillMaxWidth().statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Column {
            Text(greeting, style = MaterialTheme.typography.bodySmall, color = IvoryDim.copy(0.6f))
            Text(
                text       = if (userName != null) "Welcome, $userName 👑" else "Kila Darbar",
                style      = MaterialTheme.typography.titleLarge,
                color      = Gold,
                fontWeight = FontWeight.Bold,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = {}) {
                Icon(Icons.Filled.Search, null, tint = IvoryDim, modifier = Modifier.size(22.dp))
            }
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape)
                    .background(LuxuryGradients.goldHorizontal).clickable { onProfileClick() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text       = userName?.firstOrNull()?.uppercaseChar()?.toString() ?: "G",
                    color      = Obsidian,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 15.sp,
                )
            }
        }
    }
}

/* ── API-driven hero pager ── */
@Composable
private fun HeroBannerPager(banners: List<UiBanner>, onMenuClick: () -> Unit) {
    if (banners.isEmpty()) return
    val pagerState = rememberPagerState { banners.size }

    LaunchedEffect(banners.size) {
        while (true) {
            delay(3500)
            pagerState.animateScrollToPage(
                (pagerState.currentPage + 1) % banners.size,
                animationSpec = tween(700, easing = FastOutSlowInEasing),
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(240.dp)
            .clip(RoundedCornerShape(24.dp)),
    ) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            HeroBannerSlide(banner = banners[page], onClick = onMenuClick)
        }

        // Page indicator
        Row(
            modifier              = Modifier.align(Alignment.BottomCenter).padding(bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            banners.indices.forEach { i ->
                Box(
                    modifier = Modifier
                        .height(4.dp)
                        .animateContentSize(spring(Spring.DampingRatioMediumBouncy))
                        .width(if (pagerState.currentPage == i) 24.dp else 8.dp)
                        .clip(CircleShape)
                        .background(if (pagerState.currentPage == i) Gold else Ivory.copy(0.4f)),
                )
            }
        }
    }
}

@Composable
private fun HeroBannerSlide(banner: UiBanner, onClick: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(banner.id) { visible = true }

    val startColor = remember(banner.colorStart) {
        try { Color(android.graphics.Color.parseColor(banner.colorStart)) } catch (_: Exception) { MaroonDark }
    }
    val endColor = remember(banner.colorEnd) {
        try { Color(android.graphics.Color.parseColor(banner.colorEnd)) } catch (_: Exception) { Color(0xFF1A0608) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(colorStops = arrayOf(0f to startColor, 0.6f to startColor.copy(0.8f), 1f to endColor)))
            .clickable { onClick() },
    ) {
        // Background glow
        Box(
            modifier = Modifier.size(220.dp).align(Alignment.CenterEnd).offset(x = 20.dp)
                .background(Brush.radialGradient(listOf(Gold.copy(0.08f), Color.Transparent)))
        )

        // Big emoji (right side, decorative)
        Text(
            text     = banner.emoji,
            fontSize = 110.sp,
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 12.dp).alpha(0.3f).scale(1.15f),
        )

        // Content (left side)
        Column(
            modifier            = Modifier.align(Alignment.CenterStart).padding(start = 24.dp, end = 110.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Tag badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Gold.copy(0.2f))
                    .border(0.5.dp, Gold.copy(0.5f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(
                    text       = banner.tag.uppercase(),
                    style      = MaterialTheme.typography.labelSmall,
                    color      = Gold,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    fontSize   = 9.sp,
                )
            }

            // Title — animated in
            AnimatedVisibility(visible, enter = slideInVertically(tween(500)) + fadeIn(tween(500))) {
                Text(
                    text       = banner.title,
                    style      = MaterialTheme.typography.headlineMedium,
                    color      = Ivory,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 34.sp,
                    maxLines   = 2,
                )
            }

            // Subtitle
            AnimatedVisibility(visible, enter = slideInVertically(tween(500, 80)) + fadeIn(tween(500, 80))) {
                Text(
                    text     = banner.subtitle,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = IvoryDim.copy(0.75f),
                    maxLines = 1,
                )
            }

            // CTA button
            AnimatedVisibility(visible, enter = fadeIn(tween(400, 160)) + scaleIn(initialScale = 0.9f, animationSpec = spring(Spring.DampingRatioMediumBouncy))) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(LuxuryGradients.goldHorizontal)
                        .clickable { onClick() }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text          = banner.ctaText.uppercase(),
                        style         = MaterialTheme.typography.labelMedium,
                        color         = Obsidian,
                        fontWeight    = FontWeight.Bold,
                        letterSpacing = 1.sp,
                    )
                    Icon(Icons.Filled.ArrowForward, null, tint = Obsidian, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

/* ── Today's Offers — full-width auto-pager with image cards ── */
@Composable
private fun OffersMarquee(offers: List<UiOffer>) {
    if (offers.isEmpty()) return

    val pagerState = rememberPagerState { offers.size }

    // Auto-advance every 4s
    LaunchedEffect(offers.size) {
        while (true) {
            delay(4000)
            pagerState.animateScrollToPage(
                (pagerState.currentPage + 1) % offers.size,
                animationSpec = tween(600, easing = FastOutSlowInEasing),
            )
        }
    }

    Column(
        modifier            = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // Section header
        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Column {
                EyebrowLabel("Today's Offers")
                Text(
                    text       = "Exclusive deals for you",
                    style      = MaterialTheme.typography.titleMedium,
                    color      = Ivory,
                    fontWeight = FontWeight.Bold,
                )
            }
            // Dot indicators
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                offers.indices.forEach { i ->
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .animateContentSize(spring(Spring.DampingRatioMediumBouncy))
                            .width(if (pagerState.currentPage == i) 20.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (pagerState.currentPage == i) Gold else IvoryDim.copy(0.3f)),
                    )
                }
            }
        }

        // Full-width pager
        HorizontalPager(
            state            = pagerState,
            contentPadding   = PaddingValues(horizontal = 16.dp),
            pageSpacing      = 12.dp,
            modifier         = Modifier.fillMaxWidth(),
        ) { page ->
            OfferCard(offer = offers[page])
        }

        Spacer(Modifier.height(6.dp))
    }
}

@Composable
private fun OfferCard(offer: UiOffer) {
    val context = LocalContext.current

    val startColor = remember(offer.colorStart) {
        try { Color(android.graphics.Color.parseColor(offer.colorStart)) } catch (_: Exception) { Maroon }
    }
    val endColor = remember(offer.colorEnd) {
        try { Color(android.graphics.Color.parseColor(offer.colorEnd)) } catch (_: Exception) { MaroonDark }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.linearGradient(listOf(startColor, endColor))),
    ) {
        // Food image (right half)
        if (!offer.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(offer.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = offer.title,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.55f)
                    .align(Alignment.CenterEnd),
            )
        }

        // Gradient overlay — strong on left, fades right
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colorStops = arrayOf(
                            0f   to startColor,
                            0.45f to startColor.copy(alpha = 0.92f),
                            0.70f to startColor.copy(alpha = 0.5f),
                            1f   to Color.Transparent,
                        )
                    )
                )
        )

        // Content overlay — left side
        Column(
            modifier            = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.62f)
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Top: badge
            if (offer.badgeText.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Gold.copy(0.25f))
                        .border(0.5.dp, Gold.copy(0.6f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(
                        text          = offer.badgeText,
                        fontSize      = 9.sp,
                        color         = Gold,
                        fontWeight    = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                    )
                }
            }

            // Middle: title + description
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text       = offer.title,
                    style      = MaterialTheme.typography.titleLarge,
                    color      = Ivory,
                    fontWeight = FontWeight.Bold,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                    lineHeight = 26.sp,
                )
                Text(
                    text     = offer.description,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = IvoryDim.copy(0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp,
                )
            }

            // Bottom: promo code + savings
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                if (offer.promoCode.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Obsidian.copy(0.45f))
                            .border(0.5.dp, Gold.copy(0.6f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Icon(
                            imageVector        = Icons.Filled.ContentCopy,
                            contentDescription = null,
                            tint               = Gold,
                            modifier           = Modifier.size(11.dp),
                        )
                        Text(
                            text          = offer.promoCode,
                            fontSize      = 11.sp,
                            color         = Gold,
                            fontWeight    = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                        )
                    }
                }
                if (offer.savingText.isNotEmpty()) {
                    Text(
                        text       = offer.savingText,
                        fontSize   = 11.sp,
                        color      = Color(0xFF6EE085),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        // Emoji watermark — right edge
        Text(
            text     = offer.emoji,
            fontSize = 56.sp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 8.dp)
                .alpha(if (offer.imageUrl.isNullOrBlank()) 0.3f else 0f),
        )
    }
}

/* ── Quick actions ── */
@Composable
private fun QuickActionsRow(onMenuClick: () -> Unit) {
    Row(
        modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        QUICK_ACTIONS.forEachIndexed { i, action ->
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { delay(i * 80L); visible = true }
            AnimatedVisibility(visible, enter = fadeIn(tween(400)) + scaleIn(spring(Spring.DampingRatioMediumBouncy), 0.7f), modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(action.bg.copy(0.2f))
                        .border(1.dp, action.bg.copy(0.35f), RoundedCornerShape(16.dp))
                        .clickable { onMenuClick() }
                        .padding(vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier         = Modifier.size(36.dp).clip(CircleShape).background(action.bg.copy(0.3f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(action.icon, null, tint = Ivory.copy(0.9f), modifier = Modifier.size(18.dp))
                    }
                    Text(action.label, style = MaterialTheme.typography.labelSmall, color = IvoryDim, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

/* ── Party Hall CTA ── */
@Composable
private fun PartyHallCta(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.linearGradient(colorStops = arrayOf(0f to Color(0xFF4A148C), 0.55f to Color(0xFF2A0A4A), 1f to Color(0xFF1A0608))))
            .border(1.dp, GoldBorder, RoundedCornerShape(22.dp))
            .clickable { onClick() }
            .padding(18.dp),
    ) {
        Box(
            Modifier.size(160.dp).align(Alignment.CenterEnd).offset(x = 24.dp)
                .background(Brush.radialGradient(listOf(Gold.copy(0.10f), Color.Transparent)))
        )
        Text("🎉", fontSize = 72.sp, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp).alpha(0.3f))
        Column(
            modifier            = Modifier.align(Alignment.CenterStart).padding(end = 90.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RoyalBadge("Party Hall ✦", isGold = true)
            Text(
                "Host Your Celebration",
                style      = MaterialTheme.typography.titleLarge,
                color      = Ivory,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Birthdays, ring ceremonies & get-togethers up to 100 guests",
                style    = MaterialTheme.typography.bodySmall,
                color    = IvoryDim.copy(0.75f),
                maxLines = 2,
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(LuxuryGradients.goldHorizontal)
                    .clickable { onClick() }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("Explore Packages", style = MaterialTheme.typography.labelMedium, color = Obsidian, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                Icon(Icons.Filled.ArrowForward, null, tint = Obsidian, modifier = Modifier.size(14.dp))
            }
        }
    }
}

/* ── API-driven categories ── */
@Composable
private fun CategoryChips(categories: List<UiCategory>, onCategoryClick: (Int) -> Unit) {
    var selectedId by remember { mutableIntStateOf(-1) }
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(categories) { cat ->
            val selected = selectedId == cat.id
            val scale by animateFloatAsState(if (selected) 1.06f else 1f, spring(Spring.DampingRatioMediumBouncy), label = "catScale")
            Column(
                modifier = Modifier.scale(scale)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (selected) Gold.copy(0.15f) else Surface1)
                    .border(1.dp, if (selected) Gold.copy(0.5f) else GoldBorder, RoundedCornerShape(18.dp))
                    .clickable { selectedId = cat.id; onCategoryClick(cat.id) }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(cat.emoji, fontSize = 28.sp)
                Text(cat.name, style = MaterialTheme.typography.labelSmall, color = if (selected) Gold else IvoryDim, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
                if (cat.count > 0) Text("${cat.count} items", style = MaterialTheme.typography.labelSmall, color = IvoryDim.copy(0.4f), fontSize = 9.sp)
            }
        }
    }
}

@Composable
private fun CategoryChipsShimmer() {
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(5) { ShimmerBox(modifier = Modifier.size(80.dp, 90.dp), shape = RoundedCornerShape(18.dp)) }
    }
}

/* ── Today's Special ── */
@Composable
private fun TodaysSpecial(item: UiMenuItem, onItemClick: (String) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)) {
        EyebrowLabel("Today's Spotlight")
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(160.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(colorStops = arrayOf(0f to MaroonDark, 0.5f to Color(0xFF3D0F1A), 1f to Color(0xFF1A0608))))
                .border(1.dp, GoldBorder, RoundedCornerShape(24.dp))
                .clickable { onItemClick(item.id) }
                .padding(20.dp),
        ) {
            Box(modifier = Modifier.size(180.dp).align(Alignment.CenterEnd)
                .background(Brush.radialGradient(listOf(Gold.copy(0.08f), Color.Transparent))))
            Column(modifier = Modifier.align(Alignment.CenterStart)) {
                RoyalBadge("Chef's Selection ✦", isGold = true)
                Spacer(Modifier.height(8.dp))
                Text(item.name, style = MaterialTheme.typography.headlineSmall, color = Ivory, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(item.description, style = MaterialTheme.typography.bodySmall, color = IvoryDim.copy(0.65f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("₹${item.discountPrice ?: item.price}", style = MaterialTheme.typography.titleMedium, color = Gold, fontWeight = FontWeight.Bold)
                    if (item.discountPrice != null && item.discountPrice < item.price) {
                        val pct = ((item.price - item.discountPrice) / item.price * 100).toInt()
                        RoyalBadge("Save $pct%", isGold = false)
                    }
                }
            }
            Text(menuEmoji(item.name), fontSize = 80.sp, modifier = Modifier.align(Alignment.CenterEnd).alpha(0.35f))
        }
    }
}

/* ── Best sellers grid ── */
@Composable
private fun BestSellersGrid(
    items:          List<UiMenuItem>,
    onItemClick:    (String) -> Unit,
    cartQuantities: Map<String, Int>,
    onAdd:          (UiMenuItem) -> Unit,
    onIncrement:    (UiMenuItem) -> Unit,
    onDecrement:    (UiMenuItem) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.chunked(2).forEachIndexed { rowIdx, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEachIndexed { colIdx, item ->
                    val qty = cartQuantities[item.id] ?: 0
                    BestSellerCard(
                        item       = item,
                        qty        = qty,
                        delay      = (rowIdx * 2 + colIdx) * 60,
                        onClick    = { onItemClick(item.id) },
                        onAdd      = { onAdd(item) },
                        onIncrement = { onIncrement(item) },
                        onDecrement = { onDecrement(item) },
                        modifier   = Modifier.weight(1f),
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun BestSellerCard(
    item:        UiMenuItem,
    qty:         Int,
    delay:       Int,
    onClick:     () -> Unit,
    onAdd:       () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier:    Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(delay.toLong()); visible = true }

    AnimatedVisibility(visible, enter = fadeIn(tween(500)) + scaleIn(spring(Spring.DampingRatioMediumBouncy), 0.85f), modifier = modifier) {
        LuxuryCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(130.dp)
                    .background(Brush.radialGradient(listOf(Maroon.copy(0.5f), Surface1))),
                contentAlignment = Alignment.Center,
            ) {
                Text(menuEmoji(item.name), fontSize = 64.sp)
                Box(modifier = Modifier.align(Alignment.TopStart).padding(8.dp)) { RoyalBadge(item.tag, isGold = true) }
                Box(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp))   { FoodTypeIndicator(item.isVeg, Modifier.size(16.dp)) }
            }
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(item.name, style = MaterialTheme.typography.titleSmall, color = Ivory, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (item.rating > 0f) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.Star, null, tint = Gold, modifier = Modifier.size(12.dp))
                        Text("%.1f".format(item.rating), style = MaterialTheme.typography.labelSmall, color = Gold, fontWeight = FontWeight.SemiBold)
                    }
                }
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("₹${item.discountPrice ?: item.price}", style = MaterialTheme.typography.titleSmall, color = Ivory, fontWeight = FontWeight.Bold)
                    AnimatedAddButton(qty, onAdd, onIncrement, onDecrement)
                }
            }
        }
    }
}

@Composable
private fun BestSellersShimmer() {
    Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(2) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(2) { ShimmerBox(modifier = Modifier.weight(1f).height(200.dp), shape = RoundedCornerShape(20.dp)) }
            }
        }
    }
}

/* ── Error / Loyalty helpers ── */
@Composable
private fun ErrorRetry(message: String, onRetry: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(32.dp), Arrangement.spacedBy(12.dp), Alignment.CenterHorizontally) {
        Text("⚠️", fontSize = 40.sp)
        Text(message, style = MaterialTheme.typography.bodySmall, color = IvoryDim.copy(0.6f), maxLines = 2)
        GoldButton("Retry", onRetry, Modifier.width(120.dp))
    }
}

@Composable
private fun LoyaltySection(points: Int, tier: String) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(colorStops = arrayOf(0f to GoldDark.copy(0.15f), 1f to MaroonDark.copy(0.2f))))
            .border(1.dp, Gold.copy(0.25f), RoundedCornerShape(24.dp))
            .padding(20.dp),
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                EyebrowLabel("Royal Rewards")
                Text("$points pts", style = MaterialTheme.typography.headlineSmall, color = Gold, fontWeight = FontWeight.Bold)
                Text("🏆 $tier Member · Redeem now →", style = MaterialTheme.typography.bodySmall, color = IvoryDim.copy(0.65f))
            }
            Text("👑", fontSize = 48.sp)
        }
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
