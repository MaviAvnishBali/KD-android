package com.kiladarbar.data.remote.dto

import com.google.gson.annotations.SerializedName

// ── Generic wrappers ─────────────────────────────────────────────────────────

data class ApiResponse<T>(
    @SerializedName("success")   val success:   Boolean,
    @SerializedName("message")   val message:   String?,
    @SerializedName("data")      val data:      T?,
    @SerializedName("errors")    val errors:    List<String>?,
    @SerializedName("timestamp") val timestamp: String?,
    @SerializedName("traceId")   val traceId:   String?,
)

data class PagedResponse<T>(
    @SerializedName("content")          val content:          List<T>,
    @SerializedName("totalElements")    val totalElements:    Long,
    @SerializedName("totalPages")       val totalPages:       Int,
    @SerializedName("number")           val number:           Int,
    @SerializedName("size")             val size:             Int,
    @SerializedName("first")            val first:            Boolean,
    @SerializedName("last")             val last:             Boolean,
    @SerializedName("numberOfElements") val numberOfElements: Int,
)

// ── Auth ─────────────────────────────────────────────────────────────────────

data class SendOtpRequest(
    @SerializedName("phone")   val phone: String,
    @SerializedName("purpose") val purpose: String = "LOGIN",
)

data class VerifyOtpRequest(
    @SerializedName("phone") val phone: String,
    @SerializedName("otp")   val otp: String,
)

data class GoogleAuthRequest(@SerializedName("idToken") val idToken: String)

data class RefreshTokenRequest(@SerializedName("refreshToken") val refreshToken: String)

data class GuestLoginRequest(@SerializedName("deviceId") val deviceId: String? = null)

data class AuthResponse(
    @SerializedName("accessToken")  val accessToken:  String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("tokenType")    val tokenType:    String,
    @SerializedName("expiresIn")    val expiresIn:    Long,
    @SerializedName("user")         val user:         UserInfo,
) {
    data class UserInfo(
        @SerializedName("id")       val id:       String,
        @SerializedName("name")     val name:     String?,
        @SerializedName("phone")    val phone:    String?,
        @SerializedName("email")    val email:    String?,
        @SerializedName("role")     val role:     String,
        @SerializedName("verified") val verified: Boolean,
    )
}

// ── Menu ─────────────────────────────────────────────────────────────────────

data class CategoryDto(
    @SerializedName("id")           val id:           Int,
    @SerializedName("name")         val name:         String,
    @SerializedName("slug")         val slug:         String,
    @SerializedName("description")  val description:  String?,
    @SerializedName("imageUrl")     val imageUrl:     String?,
    @SerializedName("displayOrder") val displayOrder: Int,
    @SerializedName("itemCount")    val itemCount:    Int?,
)

data class MenuItemDto(
    @SerializedName("id")              val id:              String,
    @SerializedName("categoryId")      val categoryId:      Int,
    @SerializedName("name")            val name:            String,
    @SerializedName("slug")            val slug:            String,
    @SerializedName("description")     val description:     String?,
    @SerializedName("price")           val price:           Double,
    @SerializedName("discountPrice")   val discountPrice:   Double?,
    @SerializedName("foodType")        val foodType:        String,
    @SerializedName("gstRate")         val gstRate:         Double,
    @SerializedName("preparationTime") val preparationTime: Int?,
    @SerializedName("calories")        val calories:        Int?,
    @SerializedName("isAvailable")     val isAvailable:     Boolean,
    @SerializedName("isBestSeller")    val isBestSeller:    Boolean,
    @SerializedName("isRecommended")   val isRecommended:   Boolean,
    @SerializedName("rating")          val rating:          Double?,
    @SerializedName("totalReviews")    val totalReviews:    Int?,
    @SerializedName("images")          val images:          List<ItemImageDto>?,
    @SerializedName("tags")            val tags:            List<String>?,
)

data class ItemImageDto(
    @SerializedName("url")       val url:       String,
    @SerializedName("isPrimary") val isPrimary: Boolean,
)

// ── Cart ─────────────────────────────────────────────────────────────────────

data class CartDto(
    @SerializedName("userId")            val userId:            String,
    @SerializedName("items")             val items:             List<CartItemDto>,
    @SerializedName("itemCount")         val itemCount:         Int,
    @SerializedName("subtotal")          val subtotal:          Double,
    @SerializedName("discountAmount")    val discountAmount:    Double,
    @SerializedName("gstAmount")         val gstAmount:         Double,
    @SerializedName("deliveryCharge")    val deliveryCharge:    Double,
    @SerializedName("totalAmount")       val totalAmount:       Double,
    @SerializedName("appliedCoupon")     val appliedCoupon:     String?,
    @SerializedName("freeDeliveryAbove") val freeDeliveryAbove: Double,
)

data class CartItemDto(
    @SerializedName("menuItemId")          val menuItemId:          String,
    @SerializedName("name")                val name:                String,
    @SerializedName("imageUrl")            val imageUrl:            String?,
    @SerializedName("unitPrice")           val unitPrice:           Double,
    @SerializedName("quantity")            val quantity:            Int,
    @SerializedName("totalPrice")          val totalPrice:          Double,
    @SerializedName("specialInstruction")  val specialInstruction:  String?,
)

data class AddCartItemRequest(
    @SerializedName("menuItemId")         val menuItemId:         String,
    @SerializedName("quantity")           val quantity:           Int = 1,
    @SerializedName("specialInstruction") val specialInstruction: String? = null,
)

data class UpdateCartItemRequest(@SerializedName("quantity") val quantity: Int)

// ── Orders ───────────────────────────────────────────────────────────────────

data class OrderDto(
    @SerializedName("id")                val id:               String,
    @SerializedName("orderNumber")       val orderNumber:      String,
    @SerializedName("status")            val status:           String,
    @SerializedName("orderType")         val orderType:        String,
    @SerializedName("items")             val items:            List<OrderItemDto>,
    @SerializedName("subtotal")          val subtotal:         Double,
    @SerializedName("discountAmount")    val discountAmount:   Double,
    @SerializedName("deliveryCharge")    val deliveryCharge:   Double,
    @SerializedName("cgstAmount")        val cgstAmount:       Double,
    @SerializedName("sgstAmount")        val sgstAmount:       Double,
    @SerializedName("totalAmount")       val totalAmount:      Double,
    @SerializedName("couponCode")        val couponCode:       String?,
    @SerializedName("pointsEarned")      val pointsEarned:     Int,
    @SerializedName("estimatedMinutes")  val estimatedMinutes: Int?,
    @SerializedName("deliveryPartner")   val deliveryPartner:  DeliveryPartnerDto?,
    @SerializedName("createdAt")         val createdAt:        String,
    @SerializedName("confirmedAt")       val confirmedAt:      String?,
    @SerializedName("deliveredAt")       val deliveredAt:      String?,
)

data class OrderItemDto(
    @SerializedName("id")         val id:        String,
    @SerializedName("name")       val name:      String,
    @SerializedName("quantity")   val quantity:  Int,
    @SerializedName("unitPrice")  val unitPrice: Double,
    @SerializedName("totalPrice") val totalPrice: Double,
    @SerializedName("kdsStatus")  val kdsStatus: String,
)

data class DeliveryPartnerDto(
    @SerializedName("name")          val name:          String,
    @SerializedName("phone")         val phone:         String,
    @SerializedName("vehicleNumber") val vehicleNumber: String?,
    @SerializedName("rating")        val rating:        Double,
    @SerializedName("currentLat")    val currentLat:    Double?,
    @SerializedName("currentLng")    val currentLng:    Double?,
)

data class PlaceOrderRequest(
    @SerializedName("branchId")     val branchId:     String,
    @SerializedName("orderType")    val orderType:    String,
    @SerializedName("items")        val items:        List<OrderItemRequest>,
    @SerializedName("addressId")    val addressId:    String?  = null,
    @SerializedName("couponCode")   val couponCode:   String?  = null,
    @SerializedName("redeemPoints") val redeemPoints: Int?     = null,
    @SerializedName("instructions") val instructions: String?  = null,
)

data class OrderItemRequest(
    @SerializedName("menuItemId")         val menuItemId:         String,
    @SerializedName("quantity")           val quantity:           Int,
    @SerializedName("specialInstruction") val specialInstruction: String? = null,
)

data class RateOrderRequest(
    @SerializedName("foodRating")        val foodRating:        Int,
    @SerializedName("deliveryRating")    val deliveryRating:    Int?,
    @SerializedName("restaurantRating")  val restaurantRating:  Int,
    @SerializedName("comment")           val comment:           String?,
)

// ── Payments ─────────────────────────────────────────────────────────────────

data class InitiatePaymentRequest(
    @SerializedName("orderId") val orderId: String,
    @SerializedName("method")  val method: String,
)

data class PaymentInitDto(
    @SerializedName("gatewayOrderId") val gatewayOrderId: String,
    @SerializedName("amount")         val amount:         Double,
    @SerializedName("currency")       val currency:       String,
    @SerializedName("keyId")          val keyId:          String,
)

data class VerifyPaymentRequest(
    @SerializedName("orderId")           val orderId:           String,
    @SerializedName("gatewayOrderId")    val gatewayOrderId:    String,
    @SerializedName("gatewayPaymentId")  val gatewayPaymentId:  String,
    @SerializedName("gatewaySignature")  val gatewaySignature:  String,
)

// ── Reservations ─────────────────────────────────────────────────────────────

data class CreateReservationRequest(
    @SerializedName("branchId")       val branchId:       String,
    @SerializedName("customerName")   val customerName:   String,
    @SerializedName("customerPhone")  val customerPhone:  String,
    @SerializedName("partySize")      val partySize:      Int,
    @SerializedName("reservedDate")   val reservedDate:   String,
    @SerializedName("reservedTime")   val reservedTime:   String,
    @SerializedName("occasion")       val occasion:       String? = null,
    @SerializedName("specialRequest") val specialRequest: String? = null,
)

data class ReservationDto(
    @SerializedName("id")             val id:             String,
    @SerializedName("branchName")     val branchName:     String?,
    @SerializedName("customerName")   val customerName:   String,
    @SerializedName("customerPhone")  val customerPhone:  String,
    @SerializedName("partySize")      val partySize:      Int,
    @SerializedName("reservedDate")   val reservedDate:   String,
    @SerializedName("reservedTime")   val reservedTime:   String,
    @SerializedName("occasion")       val occasion:       String?,
    @SerializedName("specialRequest") val specialRequest: String?,
    @SerializedName("status")         val status:         String,
    @SerializedName("tableNumber")    val tableNumber:    String?,
)

// ── Profile / Addresses ──────────────────────────────────────────────────────

data class UserProfileDto(
    @SerializedName("id")              val id:              String,
    @SerializedName("name")            val name:            String?,
    @SerializedName("phone")           val phone:           String?,
    @SerializedName("email")           val email:           String?,
    @SerializedName("avatarUrl")       val avatarUrl:       String?,
    @SerializedName("dateOfBirth")     val dateOfBirth:     String?,
    @SerializedName("anniversaryDate") val anniversaryDate: String?,
    @SerializedName("gender")          val gender:          String?,
    @SerializedName("role")            val role:            String,
    @SerializedName("verified")        val verified:        Boolean,
    @SerializedName("createdAt")       val createdAt:       String?,
)

data class UpdateProfileRequest(
    @SerializedName("name")            val name:            String? = null,
    @SerializedName("email")           val email:           String? = null,
    @SerializedName("gender")          val gender:          String? = null,
    @SerializedName("dateOfBirth")     val dateOfBirth:     String? = null,
    @SerializedName("anniversaryDate") val anniversaryDate: String? = null,
)

data class AddressDto(
    @SerializedName("id")           val id:           String?,
    @SerializedName("label")        val label:        String?,
    @SerializedName("addressLine1") val addressLine1: String,
    @SerializedName("addressLine2") val addressLine2: String?,
    @SerializedName("landmark")     val landmark:     String?,
    @SerializedName("city")         val city:         String,
    @SerializedName("state")        val state:        String,
    @SerializedName("pincode")      val pincode:      String,
    @SerializedName("latitude")     val latitude:     Double?,
    @SerializedName("longitude")    val longitude:    Double?,
    @SerializedName("default")      val isDefault:    Boolean = false,
)

data class CreateAddressRequest(
    @SerializedName("label")        val label:        String?,
    @SerializedName("addressLine1") val addressLine1: String,
    @SerializedName("addressLine2") val addressLine2: String?,
    @SerializedName("landmark")     val landmark:     String?,
    @SerializedName("city")         val city:         String,
    @SerializedName("state")        val state:        String,
    @SerializedName("pincode")      val pincode:      String,
)

// ── Banners & Offers ─────────────────────────────────────────────────────────

data class BannerDto(
    @SerializedName("id")           val id:           String,
    @SerializedName("title")        val title:        String,
    @SerializedName("subtitle")     val subtitle:     String?,
    @SerializedName("tag")          val tag:          String?,
    @SerializedName("emoji")        val emoji:        String?,
    @SerializedName("bgColorStart") val bgColorStart: String,
    @SerializedName("bgColorEnd")   val bgColorEnd:   String,
    @SerializedName("ctaText")      val ctaText:      String?,
    @SerializedName("ctaLink")      val ctaLink:      String?,
    @SerializedName("displayOrder") val displayOrder: Int,
)

data class OfferBannerDto(
    @SerializedName("id")              val id:             String,
    @SerializedName("emoji")           val emoji:          String,
    @SerializedName("title")           val title:          String,
    @SerializedName("description")     val description:    String?,
    @SerializedName("promoCode")       val promoCode:      String?,
    @SerializedName("savingText")      val savingText:     String?,
    @SerializedName("badgeText")       val badgeText:      String?,
    @SerializedName("bgColorStart")    val bgColorStart:   String,
    @SerializedName("bgColorEnd")      val bgColorEnd:     String,
    @SerializedName("imageUrl")        val imageUrl:       String?,
    @SerializedName("discountType")    val discountType:   String?,
    @SerializedName("discountValue")   val discountValue:  Double?,
    @SerializedName("minOrderAmount")  val minOrderAmount: Double?,
    @SerializedName("maxDiscount")     val maxDiscount:    Double?,
    @SerializedName("validUntil")      val validUntil:     String?,
    @SerializedName("displayOrder")    val displayOrder:   Int,
)

// ── Loyalty ───────────────────────────────────────────────────────────────────

data class LoyaltyDto(
    @SerializedName("points")         val points:         Int,
    @SerializedName("tier")           val tier:           String,
    @SerializedName("lifetimePoints") val lifetimePoints: Int,
    @SerializedName("nextTierPoints") val nextTierPoints: Int?,
    @SerializedName("nextTier")       val nextTier:       String?,
)
