package com.kiladarbar.data.repository

import com.kiladarbar.data.remote.ApiService
import com.kiladarbar.data.remote.dto.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepository @Inject constructor(private val api: ApiService) {

    suspend fun getCart(): NetworkResult<CartDto> =
        safeApiCall { api.getCart() }

    suspend fun addItem(menuItemId: String, qty: Int = 1, note: String? = null): NetworkResult<CartDto> =
        safeApiCall { api.addToCart(AddCartItemRequest(menuItemId, qty, note)) }

    suspend fun updateItem(itemId: String, qty: Int): NetworkResult<CartDto> =
        safeApiCall { api.updateCartItem(itemId, UpdateCartItemRequest(qty)) }

    suspend fun removeItem(itemId: String): NetworkResult<CartDto> =
        safeApiCall { api.removeCartItem(itemId) }

    suspend fun clearCart(): NetworkResult<Unit> =
        safeApiCall { api.clearCart() }

    suspend fun applyCoupon(code: String): NetworkResult<CartDto> =
        safeApiCall { api.applyCoupon(code) }

    suspend fun removeCoupon(): NetworkResult<CartDto> =
        safeApiCall { api.removeCoupon() }
}
