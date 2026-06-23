package com.kiladarbar.ui.models

import com.kiladarbar.data.remote.dto.BannerDto
import com.kiladarbar.data.remote.dto.CartItemDto
import com.kiladarbar.data.remote.dto.CategoryDto
import com.kiladarbar.data.remote.dto.MenuItemDto
import com.kiladarbar.data.remote.dto.OfferBannerDto
import com.kiladarbar.data.remote.dto.OrderDto

/* ── Shared display-level models ── */

data class UiMenuItem(
    val id:              String,
    val name:            String,
    val description:     String,
    val price:           Double,
    val discountPrice:   Double?,
    val imageUrl:        String?,
    val isVeg:           Boolean,
    val foodType:        String,
    val categoryId:      Int,
    val tag:             String,
    val rating:          Float,
    val isAvailable:     Boolean,
    val isBestSeller:    Boolean,
    val preparationTime: Int?,
    val calories:        Int?     = null,
)

data class UiCategory(
    val id:       Int,
    val name:     String,
    val emoji:    String,
    val imageUrl: String?,
    val count:    Int,
)

data class UiCartItem(
    val menuItemId:         String,
    val name:               String,
    val imageUrl:           String?,
    val unitPrice:          Double,
    val quantity:           Int,
    val totalPrice:         Double,
    val specialInstruction: String?,
)

data class UiCart(
    val items:          List<UiCartItem>,
    val itemCount:      Int,
    val subtotal:       Double,
    val discountAmount: Double,
    val gstAmount:      Double,
    val deliveryCharge: Double,
    val totalAmount:    Double,
    val appliedCoupon:  String?,
    val freeDeliveryAbove: Double,
)

data class UiOrder(
    val id:              String,
    val orderNumber:     String,
    val status:          String,
    val emoji:           String,
    val title:           String,
    val itemCount:       Int,
    val totalAmount:     Double,
    val date:            String,
    val isDelivered:     Boolean,
    val isCancelled:     Boolean,
)

data class UiBanner(
    val id:          String,
    val title:       String,
    val subtitle:    String,
    val tag:         String,
    val emoji:       String,
    val colorStart:  String,
    val colorEnd:    String,
    val ctaText:     String,
)

data class UiOffer(
    val id:             String,
    val emoji:          String,
    val title:          String,
    val description:    String,
    val promoCode:      String,
    val savingText:     String,
    val badgeText:      String,
    val colorStart:     String,
    val colorEnd:       String,
    val imageUrl:       String?,
    val discountType:   String,
    val discountValue:  Double,
    val minOrderAmount: Double,
)

/* ── Mapping helpers ── */

fun BannerDto.toUiBanner() = UiBanner(
    id         = id,
    title      = title,
    subtitle   = subtitle ?: "",
    tag        = tag ?: "",
    emoji      = emoji ?: "🍽️",
    colorStart = bgColorStart,
    colorEnd   = bgColorEnd,
    ctaText    = ctaText ?: "Order Now",
)

fun OfferBannerDto.toUiOffer() = UiOffer(
    id             = id,
    emoji          = emoji,
    title          = title,
    description    = description ?: "",
    promoCode      = promoCode ?: "",
    savingText     = savingText ?: "",
    badgeText      = badgeText ?: "",
    colorStart     = bgColorStart,
    colorEnd       = bgColorEnd,
    imageUrl       = imageUrl,
    discountType   = discountType ?: "FLAT",
    discountValue  = discountValue ?: 0.0,
    minOrderAmount = minOrderAmount ?: 0.0,
)

fun MenuItemDto.toUiMenuItem(): UiMenuItem = UiMenuItem(
    id              = id,
    name            = name,
    description     = description ?: "",
    price           = price,
    discountPrice   = discountPrice,
    imageUrl        = images?.firstOrNull { it.isPrimary }?.url ?: images?.firstOrNull()?.url,
    isVeg           = foodType == "VEG" || foodType == "VEGAN" || foodType == "JAIN",
    foodType        = foodType,
    categoryId      = categoryId,
    tag             = when { isBestSeller -> "Bestseller"; isRecommended -> "Chef's Pick"; else -> "New" },
    rating          = rating?.toFloat() ?: 0f,
    isAvailable     = isAvailable,
    isBestSeller    = isBestSeller,
    preparationTime = preparationTime,
    calories        = calories,
)

fun CategoryDto.toUiCategory(): UiCategory = UiCategory(
    id       = id,
    name     = name,
    emoji    = categoryEmoji(name),
    imageUrl = imageUrl,
    count    = itemCount ?: 0,
)

fun OrderDto.toUiOrder(): UiOrder = UiOrder(
    id          = id,
    orderNumber = orderNumber,
    status      = status,
    emoji       = if (items.isNotEmpty()) menuItemEmoji(items.first().name) else "🍽️",
    title       = if (items.size == 1) items.first().name
                  else "${items.first().name} + ${items.size - 1} more",
    itemCount   = items.sumOf { it.quantity },
    totalAmount = totalAmount,
    date        = createdAt,
    isDelivered = status == "DELIVERED",
    isCancelled = status == "CANCELLED",
)

private fun categoryEmoji(name: String): String = when {
    name.contains("biryani", ignoreCase = true) -> "🍚"
    name.contains("kebab",   ignoreCase = true) -> "🔥"
    name.contains("gravy",   ignoreCase = true) ||
    name.contains("curry",   ignoreCase = true) -> "🥘"
    name.contains("bread",   ignoreCase = true) ||
    name.contains("roti",    ignoreCase = true) ||
    name.contains("naan",    ignoreCase = true) -> "🫓"
    name.contains("dessert", ignoreCase = true) ||
    name.contains("sweet",   ignoreCase = true) -> "🍮"
    name.contains("drink",   ignoreCase = true) ||
    name.contains("beverage",ignoreCase = true) -> "🍹"
    name.contains("starter", ignoreCase = true) -> "🥗"
    name.contains("rice",    ignoreCase = true) -> "🍚"
    else -> "🍽️"
}

private fun menuItemEmoji(name: String): String = when {
    name.contains("biryani", ignoreCase = true) -> "🍚"
    name.contains("paneer",  ignoreCase = true) -> "🧀"
    name.contains("dal",     ignoreCase = true) -> "🥘"
    name.contains("roti",    ignoreCase = true) ||
    name.contains("naan",    ignoreCase = true) -> "🫓"
    name.contains("lassi",   ignoreCase = true) ||
    name.contains("drink",   ignoreCase = true) -> "🍹"
    name.contains("gulab",   ignoreCase = true) ||
    name.contains("halwa",   ignoreCase = true) ||
    name.contains("tukda",   ignoreCase = true) -> "🍮"
    else -> "🍽️"
}
