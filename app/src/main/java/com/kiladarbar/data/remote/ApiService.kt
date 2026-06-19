package com.kiladarbar.data.remote

import com.kiladarbar.data.remote.dto.*
import retrofit2.http.*

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

    // ── Menu ─────────────────────────────────────────────────────────────────
    @GET(ApiEndpoints.CATEGORIES)
    suspend fun getCategories(): ApiResponse<List<CategoryDto>>

    @GET(ApiEndpoints.MENU_ITEMS)
    suspend fun getMenuItems(
        @Query("categoryId") categoryId: Int? = null,
        @Query("search")     search: String? = null,
    ): ApiResponse<List<MenuItemDto>>

    @GET(ApiEndpoints.BEST_SELLERS)
    suspend fun getBestSellers(): ApiResponse<List<MenuItemDto>>

    @GET(ApiEndpoints.RECOMMENDED)
    suspend fun getRecommended(): ApiResponse<List<MenuItemDto>>

    @GET(ApiEndpoints.MENU_ITEM_BY_ID)
    suspend fun getMenuItem(@Path("id") id: String): ApiResponse<MenuItemDto>

    // ── Orders ───────────────────────────────────────────────────────────────
    @GET(ApiEndpoints.ORDERS)
    suspend fun getOrders(): ApiResponse<List<OrderDto>>

    @GET(ApiEndpoints.ORDER_BY_ID)
    suspend fun getOrder(@Path("id") id: String): ApiResponse<OrderDto>

    @POST(ApiEndpoints.ORDERS)
    suspend fun placeOrder(@Body request: PlaceOrderRequest): ApiResponse<OrderDto>

    @PATCH(ApiEndpoints.CANCEL_ORDER)
    suspend fun cancelOrder(@Path("id") id: String): ApiResponse<Unit>

    // ── Profile ──────────────────────────────────────────────────────────────
    @GET(ApiEndpoints.PROFILE)
    suspend fun getProfile(): ApiResponse<Map<String, Any>>
}
