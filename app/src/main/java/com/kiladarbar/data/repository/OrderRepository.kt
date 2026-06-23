package com.kiladarbar.data.repository

import com.kiladarbar.data.remote.ApiService
import com.kiladarbar.data.remote.dto.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(private val api: ApiService) {

    suspend fun getOrders(page: Int = 0): NetworkResult<PagedResponse<OrderDto>> =
        safeApiCall { api.getOrders(page) }

    suspend fun getOrder(id: String): NetworkResult<OrderDto> =
        safeApiCall { api.getOrder(id) }

    suspend fun placeOrder(request: PlaceOrderRequest): NetworkResult<OrderDto> =
        safeApiCall { api.placeOrder(request) }

    suspend fun cancelOrder(id: String): NetworkResult<Unit> =
        safeApiCall { api.cancelOrder(id) }

    suspend fun rateOrder(id: String, request: RateOrderRequest): NetworkResult<Unit> =
        safeApiCall { api.rateOrder(id, request) }
}
