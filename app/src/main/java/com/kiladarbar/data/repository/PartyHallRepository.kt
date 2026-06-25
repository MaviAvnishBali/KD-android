package com.kiladarbar.data.repository

import com.kiladarbar.data.remote.ApiService
import com.kiladarbar.data.remote.dto.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PartyHallRepository @Inject constructor(private val api: ApiService) {

    suspend fun book(request: CreatePartyHallBookingRequest): NetworkResult<PartyHallBookingDto> =
        safeApiCall { api.bookPartyHall(request) }

    suspend fun myBookings(): NetworkResult<List<PartyHallBookingDto>> =
        safeApiCall { api.getMyPartyHallBookings() }

    suspend fun cancel(id: String): NetworkResult<Unit> =
        safeApiCall { api.cancelPartyHallBooking(id) }
}
