package com.kiladarbar.data.remote

import com.kiladarbar.data.remote.dto.*
import retrofit2.http.*

/**
 * Retrofit API interface — mirrors the backend's /api/v1 endpoints.
 * All functions are suspend-based for Coroutines support.
 */
interface ApiService {

    // ── Auth ─────────────────────────────────────────────────────────────────
    @POST(ApiEndpoints.SEND_OTP)
    suspend fun sendOtp(@Body request: SendOtpRequest): ApiResponse<Unit>

    @POST(ApiEndpoints.VERIFY_OTP)
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): ApiResponse<AuthResponse>

    @POST(ApiEndpoints.GOOGLE_AUTH)
    suspend fun googleLogin(@Body request: GoogleAuthRequest): ApiResponse<AuthResponse>

    @POST(ApiEndpoints.REFRESH)
    suspend fun refreshToken(@Body request: RefreshTokenRequest): ApiResponse<AuthResponse>

    @POST(ApiEndpoints.LOGOUT)
    suspend fun logout(): ApiResponse<Unit>

    @POST(ApiEndpoints.GUEST_LOGIN)
    suspend fun guestLogin(@Body request: GuestLoginRequest): ApiResponse<AuthResponse>

    @POST(ApiEndpoints.FIREBASE_PHONE)
    suspend fun verifyFirebasePhone(@Body body: Map<String, String>): ApiResponse<AuthResponse>

    // ── User / Profile ───────────────────────────────────────────────────────
    @GET(ApiEndpoints.PROFILE)
    suspend fun getProfile(): ApiResponse<UserProfileDto>

    @PUT(ApiEndpoints.PROFILE)
    suspend fun updateProfile(@Body request: UpdateProfileRequest): ApiResponse<UserProfileDto>

    @DELETE(ApiEndpoints.PROFILE)
    suspend fun deleteAccount(): ApiResponse<Unit>

    @GET(ApiEndpoints.ADDRESSES)
    suspend fun getAddresses(): ApiResponse<List<AddressDto>>

    @POST(ApiEndpoints.ADDRESSES)
    suspend fun addAddress(@Body request: CreateAddressRequest): ApiResponse<AddressDto>

    @PUT(ApiEndpoints.ADDRESS_BY_ID)
    suspend fun updateAddress(@Path("id") id: String, @Body request: CreateAddressRequest): ApiResponse<AddressDto>

    @DELETE(ApiEndpoints.ADDRESS_BY_ID)
    suspend fun deleteAddress(@Path("id") id: String): ApiResponse<Unit>

    @PUT(ApiEndpoints.ADDRESS_DEFAULT)
    suspend fun setDefaultAddress(@Path("id") id: String): ApiResponse<Unit>

    @PUT(ApiEndpoints.FCM_TOKEN)
    suspend fun updateFcmToken(@Query("token") token: String): ApiResponse<Unit>

    // ── Menu ─────────────────────────────────────────────────────────────────
    @GET(ApiEndpoints.CATEGORIES)
    suspend fun getCategories(): ApiResponse<List<CategoryDto>>

    @GET(ApiEndpoints.MENU_ITEMS)
    suspend fun getMenuItems(
        @Query("categoryId") categoryId: Int?    = null,
        @Query("search")     search: String?    = null,
        @Query("page")       page: Int          = 0,
        @Query("size")       size: Int          = 20,
    ): ApiResponse<PagedResponse<MenuItemDto>>

    @GET(ApiEndpoints.BEST_SELLERS)
    suspend fun getBestSellers(): ApiResponse<List<MenuItemDto>>

    @GET(ApiEndpoints.RECOMMENDED)
    suspend fun getRecommended(): ApiResponse<List<MenuItemDto>>

    @GET(ApiEndpoints.MENU_ITEM_BY_ID)
    suspend fun getMenuItem(@Path("id") id: String): ApiResponse<MenuItemDto>

    // ── Cart ─────────────────────────────────────────────────────────────────
    @GET(ApiEndpoints.CART)
    suspend fun getCart(): ApiResponse<CartDto>

    @POST(ApiEndpoints.CART_ITEMS)
    suspend fun addToCart(@Body request: AddCartItemRequest): ApiResponse<CartDto>

    @PUT(ApiEndpoints.CART_ITEM_BY_ID)
    suspend fun updateCartItem(@Path("id") itemId: String, @Body request: UpdateCartItemRequest): ApiResponse<CartDto>

    @DELETE(ApiEndpoints.CART_ITEM_BY_ID)
    suspend fun removeCartItem(@Path("id") itemId: String): ApiResponse<CartDto>

    @DELETE(ApiEndpoints.CART)
    suspend fun clearCart(): ApiResponse<Unit>

    @POST(ApiEndpoints.CART_COUPON)
    suspend fun applyCoupon(@Query("code") code: String): ApiResponse<CartDto>

    @DELETE(ApiEndpoints.CART_COUPON)
    suspend fun removeCoupon(): ApiResponse<CartDto>

    // ── Orders ───────────────────────────────────────────────────────────────
    @GET(ApiEndpoints.MY_ORDERS)
    suspend fun getOrders(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
    ): ApiResponse<PagedResponse<OrderDto>>

    @GET(ApiEndpoints.ORDER_BY_ID)
    suspend fun getOrder(@Path("id") id: String): ApiResponse<OrderDto>

    @POST(ApiEndpoints.ORDERS)
    suspend fun placeOrder(@Body request: PlaceOrderRequest): ApiResponse<OrderDto>

    @POST(ApiEndpoints.CANCEL_ORDER)
    suspend fun cancelOrder(@Path("id") id: String): ApiResponse<Unit>

    @POST(ApiEndpoints.RATE_ORDER)
    suspend fun rateOrder(@Path("id") id: String, @Body request: RateOrderRequest): ApiResponse<Unit>

    // ── Payments ─────────────────────────────────────────────────────────────
    @POST(ApiEndpoints.PAYMENT_CREATE)
    suspend fun createPaymentOrder(@Body request: InitiatePaymentRequest): ApiResponse<PaymentInitDto>

    @POST(ApiEndpoints.PAYMENT_VERIFY)
    suspend fun verifyPayment(@Body request: VerifyPaymentRequest): ApiResponse<Unit>

    // ── Reservations ─────────────────────────────────────────────────────────
    @POST(ApiEndpoints.RESERVATIONS)
    suspend fun createReservation(@Body request: CreateReservationRequest): ApiResponse<ReservationDto>

    @GET(ApiEndpoints.RESERVATIONS)
    suspend fun getReservations(): ApiResponse<List<ReservationDto>>

    @GET(ApiEndpoints.RESERVATION_BY_ID)
    suspend fun getReservation(@Path("id") id: String): ApiResponse<ReservationDto>

    @DELETE(ApiEndpoints.RESERVATION_BY_ID)
    suspend fun cancelReservation(@Path("id") id: String): ApiResponse<Unit>

    @GET(ApiEndpoints.RESERVATION_AVAILABILITY)
    suspend fun checkAvailability(
        @Query("branchId")  branchId: String,
        @Query("date")      date: String,
        @Query("partySize") partySize: Int,
    ): ApiResponse<List<String>>

    // ── Banners & Offers ─────────────────────────────────────────────────────────
    @GET(ApiEndpoints.BANNERS)
    suspend fun getBanners(): ApiResponse<List<BannerDto>>

    @GET(ApiEndpoints.OFFERS)
    suspend fun getOffers(): ApiResponse<List<OfferBannerDto>>

    // ── Loyalty ───────────────────────────────────────────────────────────────
    @GET(ApiEndpoints.LOYALTY)
    suspend fun getLoyalty(): ApiResponse<LoyaltyDto>
}
