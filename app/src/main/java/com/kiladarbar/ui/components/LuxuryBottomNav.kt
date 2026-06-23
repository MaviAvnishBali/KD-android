package com.kiladarbar.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kiladarbar.ui.theme.*

enum class NavTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    Home(    "Home",    Icons.Filled.Home,         Icons.Outlined.Home),
    Menu(    "Menu",    Icons.Filled.RestaurantMenu,Icons.Outlined.RestaurantMenu),
    Orders(  "Orders",  Icons.Filled.ReceiptLong,  Icons.Outlined.ReceiptLong),
    Profile( "Profile", Icons.Filled.Person,        Icons.Outlined.Person),
}

@Composable
fun LuxuryBottomNav(
    selectedTab: NavTab,
    cartCount: Int = 0,
    onTabSelected: (NavTab) -> Unit,
    onCartClick: () -> Unit,
) {
    Surface(
        color     = Color.Transparent,
        modifier  = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            // Nav pill background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Surface2),
            )

            // Nav items
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                NavTab.entries.forEachIndexed { index, tab ->
                    if (index == 2) {
                        // Cart FAB in center
                        CartFab(count = cartCount, onClick = onCartClick)
                    }
                    BottomNavItem(
                        tab       = tab,
                        selected  = selectedTab == tab,
                        onClick   = { onTabSelected(tab) },
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    tab: NavTab,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue  = if (selected) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium,
        ),
        label = "navScale",
    )
    val iconColor by animateColorAsState(
        targetValue  = if (selected) Gold else IvoryDim.copy(alpha = 0.5f),
        animationSpec = tween(200),
        label        = "navColor",
    )

    Column(
        modifier = Modifier
            .size(56.dp)
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Indicator dot
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(if (selected) Gold else Color.Transparent),
        )
        Spacer(Modifier.height(4.dp))

        Icon(
            imageVector        = if (selected) tab.selectedIcon else tab.unselectedIcon,
            contentDescription = tab.label,
            tint               = iconColor,
            modifier           = Modifier.size(22.dp),
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text  = tab.label,
            style = MaterialTheme.typography.labelSmall,
            color = iconColor,
            fontSize = 9.sp,
        )
    }
}

@Composable
private fun CartFab(count: Int, onClick: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue  = if (count > 0) 1f else 0.85f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label        = "cartFabScale",
    )

    Box(
        modifier = Modifier
            .size(52.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(LuxuryGradients.goldHorizontal)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector        = Icons.Filled.ShoppingCart,
            contentDescription = "Cart",
            tint               = Obsidian,
            modifier           = Modifier.size(22.dp),
        )
        if (count > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Maroon),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text     = if (count > 9) "9+" else count.toString(),
                    color    = Ivory,
                    fontSize = 9.sp,
                    style    = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}
