package com.kiladarbar.data.repository

import com.kiladarbar.data.remote.ApiService
import com.kiladarbar.data.remote.dto.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(private val api: ApiService) {

    suspend fun getProfile(): NetworkResult<UserProfileDto> =
        safeApiCall { api.getProfile() }

    suspend fun updateProfile(req: UpdateProfileRequest): NetworkResult<UserProfileDto> =
        safeApiCall { api.updateProfile(req) }

    suspend fun deleteAccount(): NetworkResult<Unit> =
        safeApiCall { api.deleteAccount() }

    suspend fun getAddresses(): NetworkResult<List<AddressDto>> =
        safeApiCall { api.getAddresses() }

    suspend fun addAddress(req: CreateAddressRequest): NetworkResult<AddressDto> =
        safeApiCall { api.addAddress(req) }

    suspend fun updateAddress(id: String, req: CreateAddressRequest): NetworkResult<AddressDto> =
        safeApiCall { api.updateAddress(id, req) }

    suspend fun deleteAddress(id: String): NetworkResult<Unit> =
        safeApiCall { api.deleteAddress(id) }

    suspend fun setDefaultAddress(id: String): NetworkResult<Unit> =
        safeApiCall { api.setDefaultAddress(id) }

    suspend fun updateFcmToken(token: String): NetworkResult<Unit> =
        safeApiCall { api.updateFcmToken(token) }
}
