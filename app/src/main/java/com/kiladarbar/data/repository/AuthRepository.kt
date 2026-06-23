package com.kiladarbar.data.repository

import com.kiladarbar.data.local.SessionManager
import com.kiladarbar.data.remote.ApiService
import com.kiladarbar.data.remote.dto.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val session: SessionManager,
) {
    val isLoggedIn: Flow<Boolean> = session.accessToken.map { it?.isNotBlank() == true }
    val currentUser: Flow<String?> = session.userName

    suspend fun sendOtp(phone: String): NetworkResult<Unit> =
        safeApiCall { api.sendOtp(SendOtpRequest(phone)) }

    suspend fun verifyOtp(phone: String, otp: String): NetworkResult<AuthResponse> {
        val result = safeApiCall { api.verifyOtp(VerifyOtpRequest(phone, otp)) }
        if (result is NetworkResult.Success) saveSession(result.data)
        return result
    }

    suspend fun googleLogin(idToken: String): NetworkResult<AuthResponse> {
        val result = safeApiCall { api.googleLogin(GoogleAuthRequest(idToken)) }
        if (result is NetworkResult.Success) saveSession(result.data)
        return result
    }

    suspend fun continueAsGuest(): NetworkResult<AuthResponse> {
        val result = safeApiCall { api.guestLogin(GuestLoginRequest()) }
        if (result is NetworkResult.Success) saveSession(result.data)
        return result
    }

    suspend fun refreshToken(token: String): NetworkResult<AuthResponse> {
        val result = safeApiCall { api.refreshToken(RefreshTokenRequest(token)) }
        if (result is NetworkResult.Success) saveSession(result.data)
        return result
    }

    suspend fun logout() {
        safeApiCall { api.logout() }
        session.clearSession()
    }

    private suspend fun saveSession(auth: AuthResponse) {
        session.saveSession(
            accessToken  = auth.accessToken,
            refreshToken = auth.refreshToken,
            userId       = auth.user.id,
            userName     = auth.user.name,
            phone        = auth.user.phone,
            role         = auth.user.role,
        )
    }
}
