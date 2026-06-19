package com.kiladarbar.domain.model

data class MenuItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageUrl: String? = null,
    val isVeg: Boolean = true,
    val category: String = "",
    val rating: Float = 0f,
)

data class Category(
    val id: Int = 0,
    val name: String = "",
    val imageUrl: String? = null,
)

data class CartItem(
    val menuItem: MenuItem = MenuItem(),
    val quantity: Int = 0,
)

data class Order(
    val id: String = "",
    val orderNumber: String = "",
    val status: String = "PENDING",
    val items: List<CartItem> = emptyList(),
    val total: Double = 0.0,
    val createdAt: String = "",
)

data class DriverInfo(
    val name: String = "",
    val phone: String = "",
    val vehicleNumber: String? = null,
    val rating: Float = 0f,
)
