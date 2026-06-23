package com.kiladarbar.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kiladarbar.ui.theme.*

/* ── Gold CTA button ── */
@Composable
fun GoldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue  = if (isPressed) 0.95f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label        = "goldBtnScale",
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (enabled) LuxuryGradients.goldHorizontal
                else Brush.horizontalGradient(listOf(Surface2, Surface2))
            )
            .clickable(
                interactionSource = interactionSource,
                indication        = null,
                enabled           = enabled,
                onClick           = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                },
            )
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text       = text.uppercase(),
            style      = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color      = if (enabled) Obsidian else IvoryDim,
            letterSpacing = 1.5.sp,
        )
    }
}

/* ── Outline ghost button ── */
@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, GoldBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text       = text.uppercase(),
            style      = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color      = Gold,
            letterSpacing = 1.5.sp,
        )
    }
}

/* ── Luxury card ── */
@Composable
fun LuxuryCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val elevation by animateFloatAsState(
        targetValue  = if (isPressed) 0f else 1f,
        label        = "cardElevation",
    )
    val scale by animateFloatAsState(
        targetValue  = if (isPressed) 0.97f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label        = "cardScale",
    )

    Column(
        modifier = modifier
            .scale(scale)
            .graphicsLayer { shadowElevation = elevation * 8f }
            .clip(RoundedCornerShape(20.dp))
            .background(Surface1)
            .border(1.dp, GoldBorder, RoundedCornerShape(20.dp))
            .then(
                if (onClick != null) Modifier.clickable(
                    interactionSource = interactionSource,
                    indication        = null,
                    onClick           = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClick()
                    },
                ) else Modifier
            ),
        content = content,
    )
}

/* ── Section header ── */
@Composable
fun LuxurySectionHeader(
    title: String,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.Bottom,
    ) {
        Column {
            Text(
                text       = title,
                style      = MaterialTheme.typography.titleLarge,
                color      = Ivory,
                fontWeight = FontWeight.Bold,
            )
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text  = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = IvoryDim.copy(alpha = 0.6f),
                )
            }
        }
        if (actionLabel != null && onAction != null) {
            Text(
                text      = actionLabel,
                style     = MaterialTheme.typography.labelMedium,
                color     = Gold,
                fontWeight = FontWeight.SemiBold,
                modifier  = Modifier.clickable { onAction() },
            )
        }
    }
}

/* ── Gold divider ── */
@Composable
fun GoldDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(GoldBorder),
    )
}

/* ── Veg/NonVeg indicator ── */
@Composable
fun FoodTypeIndicator(isVeg: Boolean, modifier: Modifier = Modifier) {
    val color = if (isVeg) Color(0xFF4CAF50) else Color(0xFFE53935)
    Box(
        modifier = modifier
            .size(16.dp)
            .border(1.5.dp, color, RoundedCornerShape(3.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color),
        )
    }
}

/* ── Quantity stepper ── */
@Composable
fun QuantityStepper(
    quantity: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier          = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(LuxuryGradients.goldHorizontal)
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Obsidian.copy(alpha = 0.25f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDecrement()
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Remove, null, tint = Obsidian, modifier = Modifier.size(14.dp))
        }
        Text(
            text      = quantity.toString(),
            modifier  = Modifier.padding(horizontal = 8.dp),
            style     = MaterialTheme.typography.labelLarge,
            color     = Obsidian,
            fontWeight = FontWeight.Bold,
        )
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Obsidian.copy(alpha = 0.25f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onIncrement()
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Add, null, tint = Obsidian, modifier = Modifier.size(14.dp))
        }
    }
}

/* ── Royal badge ── */
@Composable
fun RoyalBadge(
    text: String,
    modifier: Modifier = Modifier,
    isGold: Boolean = true,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(
                if (isGold) Gold.copy(alpha = 0.2f) else Maroon.copy(alpha = 0.4f)
            )
            .border(
                width  = 0.5.dp,
                color  = if (isGold) Gold.copy(alpha = 0.5f) else MaroonLight.copy(alpha = 0.5f),
                shape  = RoundedCornerShape(4.dp),
            )
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text  = text,
            style = MaterialTheme.typography.labelSmall,
            color = if (isGold) Gold else MaroonLight,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.3.sp,
        )
    }
}

/* ── Eyebrow label ── */
@Composable
fun EyebrowLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text          = text.uppercase(),
        style         = MaterialTheme.typography.labelSmall,
        color         = Gold,
        fontWeight    = FontWeight.SemiBold,
        letterSpacing = 2.sp,
        modifier      = modifier,
    )
}

/* ── Animated Add button (0 → stepper) ── */
@Composable
fun AnimatedAddButton(
    quantity: Int,
    onAdd: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
) {
    val width by animateDpAsState(
        targetValue  = if (quantity > 0) 100.dp else 80.dp,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label        = "addBtnWidth",
    )

    if (quantity == 0) {
        Box(
            modifier = Modifier
                .size(width = width, height = 36.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(LuxuryGradients.goldHorizontal)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = onAdd,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(Icons.Filled.Add, null, tint = Obsidian, modifier = Modifier.size(14.dp))
                Text("ADD", style = MaterialTheme.typography.labelSmall, color = Obsidian, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        }
    } else {
        QuantityStepper(
            quantity    = quantity,
            onIncrement = onIncrement,
            onDecrement = onDecrement,
            modifier    = Modifier.width(width).height(36.dp),
        )
    }
}

/* ── RoyalScaffold kept for compatibility ── */
@Composable
fun RoyalScaffold(
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        topBar    = topBar,
        bottomBar = bottomBar,
        containerColor = Obsidian,
        content   = content,
    )
}

/* ── SectionHeader kept for compatibility ── */
@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) = LuxurySectionHeader(title, subtitle, actionLabel, onAction, modifier)
