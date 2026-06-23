package com.kiladarbar.data.repository

import com.kiladarbar.data.remote.ApiService
import com.kiladarbar.data.remote.dto.BannerDto
import com.kiladarbar.data.remote.dto.CategoryDto
import com.kiladarbar.data.remote.dto.MenuItemDto
import com.kiladarbar.data.remote.dto.OfferBannerDto
import com.kiladarbar.data.remote.dto.PagedResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MenuRepository @Inject constructor(private val api: ApiService) {

    suspend fun getBanners(): NetworkResult<List<BannerDto>> =
        safeApiCall { api.getBanners() }

    suspend fun getOffers(): NetworkResult<List<OfferBannerDto>> =
        safeApiCall { api.getOffers() }

    suspend fun getCategories(): NetworkResult<List<CategoryDto>> =
        safeApiCall { api.getCategories() }

    suspend fun getMenuItems(
        categoryId: Int? = null,
        search: String? = null,
        page: Int = 0,
        size: Int = 20,
    ): NetworkResult<PagedResponse<MenuItemDto>> =
        safeApiCall { api.getMenuItems(categoryId, search, page, size) }

    suspend fun getBestSellers(): NetworkResult<List<MenuItemDto>> =
        safeApiCall { api.getBestSellers() }

    suspend fun getRecommended(): NetworkResult<List<MenuItemDto>> =
        safeApiCall { api.getRecommended() }

    suspend fun getMenuItem(id: String): NetworkResult<MenuItemDto> =
        safeApiCall { api.getMenuItem(id) }
}
