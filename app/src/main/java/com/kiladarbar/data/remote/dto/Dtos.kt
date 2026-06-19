package com.kiladarbar.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MenuItemDto(
    @SerializedName("id")          val id: String,
    @SerializedName("name")        val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("price")       val price: Double,
    @SerializedName("imageUrl")    val imageUrl: String?,
    @SerializedName("isVeg")       val isVeg: Boolean,
    @SerializedName("category")    val category: String,
    @SerializedName("rating")      val rating: Float = 0f,
)

data class CategoryDto(
    @SerializedName("id")       val id: Int,
    @SerializedName("name")     val name: String,
    @SerializedName("imageUrl") val imageUrl: String?,
)

data class OrderDto(
    @SerializedName("id")          val id: String,
    @SerializedName("orderNumber") val orderNumber: String,
    @SerializedName("status")      val status: String,
    @SerializedName("items")       val items: List<OrderItemDto>,
    @SerializedName("subtotal")    val subtotal: Double,
    @SerializedName("deliveryCharge") val deliveryCharge: Double,
    @SerializedName("gst")         val gst: Double,
    @SerializedName("discount")    val discount: Double,
    @SerializedName("total")       val total: Double,
    @SerializedName("createdAt")   val createdAt: String,
    @SerializedName("estimatedMinutes") val estimatedMinutes: Int?,
    @SerializedName("driverInfo")  val driverInfo: DriverInfoDto?,
)

data class OrderItemDto(
    @SerializedName("menuItem") val menuItem: MenuItemDto,
    @SerializedName("quantity") val quantity: Int,
)

data class DriverInfoDto(
    @SerializedName("name")          val name: String,
    @SerializedName("phone")         val phone: String,
    @SerializedName("vehicleNumber") val vehicleNumber: String?,
    @SerializedName("rating")        val rating: Float,
)

data class SendOtpRequest(
    @SerializedName("phone") val phone: String,
)

data class VerifyOtpRequest(
    @SerializedName("phone") val phone: String,
    @SerializedName("otp")   val otp: String,
)

data class AuthResponse(
    @SerializedName("accessToken")  val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("user")         val user: UserDto,
)

data class UserDto(
    @SerializedName("id")            val id: String,
    @SerializedName("name")          val name: String?,
    @SerializedName("phone")         val phone: String?,
    @SerializedName("email")         val email: String?,
    @SerializedName("role")          val role: String,
    @SerializedName("loyaltyPoints") val loyaltyPoints: Int = 0,
    @SerializedName("verified")      val verified: Boolean = false,
)

data class GoogleAuthRequest(
    @SerializedName("idToken") val idToken: String,
)

data class RefreshTokenRequest(
    @SerializedName("refreshToken") val refreshToken: String,
)

data class PlaceOrderRequest(
    @SerializedName("items")          val items: List<OrderItemRequest>,
    @SerializedName("deliveryAddress") val deliveryAddress: String?,
    @SerializedName("orderType")      val orderType: String,
)

data class OrderItemRequest(
    @SerializedName("menuItemId") val menuItemId: String,
    @SerializedName("quantity")   val quantity: Int,
)

data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data")    val data: T?,
    @SerializedName("message") val message: String?,
)
