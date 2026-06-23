package com.kiladarbar.ui.screens.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.PhoneAuthProvider
import com.kiladarbar.data.firebase.FirebaseAuthManager
import com.kiladarbar.data.firebase.PhoneVerificationResult
import com.kiladarbar.data.local.SessionManager
import com.kiladarbar.data.remote.ApiService
import com.kiladarbar.data.remote.dto.GuestLoginRequest
import com.kiladarbar.data.remote.dto.GoogleAuthRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading:          Boolean = false,
    val otpSent:            Boolean = false,
    val isLoggedIn:         Boolean = false,
    val error:              String? = null,
    val resendSecondsLeft:  Int     = 0,
    // Firebase Phone Auth state
    val verificationId:     String? = null,
    val isAutoVerified:     Boolean = false,   // Firebase verified without user input
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val api:            ApiService,
    private val sessionManager: SessionManager,
    private val firebase:       FirebaseAuthManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    private var resendTimerJob:    Job? = null
    private var resendToken:       PhoneAuthProvider.ForceResendingToken? = null

    /* ══════════════════════════════════════════════════════════════
       PHONE AUTH — Step 1: trigger Firebase SMS
       Call this from the Composable with the Activity reference
       ══════════════════════════════════════════════════════════════ */

    fun startPhoneVerification(phone: String, activity: Activity) {
        _uiState.update { it.copy(isLoading = true, error = null) }

        firebase.startPhoneVerification(
            phone    = phone,
            activity = activity,
            onResult = { result ->
                when (result) {
                    is PhoneVerificationResult.CodeSent -> {
                        resendToken = result.resendToken
                        _uiState.update {
                            it.copy(
                                isLoading      = false,
                                otpSent        = true,
                                verificationId = result.verificationId,
                            )
                        }
                        startResendTimer()
                    }
                    is PhoneVerificationResult.AutoVerified -> {
                        // Firebase verified the code automatically (instant verification on trusted devices)
                        viewModelScope.launch {
                            _uiState.update { it.copy(isLoading = true) }
                            finishPhoneSignIn { firebase.signInWithPhoneCredential(result.credential) }
                        }
                    }
                    is PhoneVerificationResult.Failed -> {
                        _uiState.update { it.copy(isLoading = false, error = result.message) }
                    }
                }
            },
        )
    }

    /* ══════════════════════════════════════════════════════════════
       PHONE AUTH — Step 2: user entered OTP
       ══════════════════════════════════════════════════════════════ */

    fun verifyPhoneCode(otp: String) {
        val verificationId = _uiState.value.verificationId
            ?: run { _uiState.update { it.copy(error = "Session expired. Tap Resend.") }; return }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            finishPhoneSignIn { firebase.verifyPhoneCode(verificationId, otp) }
        }
    }

    private suspend fun finishPhoneSignIn(getFirebaseToken: suspend () -> String) {
        try {
            val firebaseToken = getFirebaseToken()
            val response = api.verifyFirebasePhone(mapOf("firebaseIdToken" to firebaseToken))
            val data = response.data
            if (data != null) {
                saveSession(data.accessToken, data.refreshToken, data.user.id, data.user.name, data.user.phone, data.user.role)
                _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = response.message ?: "Verification failed") }
            }
        } catch (e: Exception) {
            val msg = when {
                e.message?.contains("invalid", ignoreCase = true) == true ||
                e.message?.contains("expired", ignoreCase = true) == true -> "Incorrect OTP. Please try again."
                e.message?.contains("too many", ignoreCase = true) == true -> "Too many attempts. Please wait."
                else -> e.message ?: "Verification failed"
            }
            _uiState.update { it.copy(isLoading = false, error = msg) }
        }
    }

    /* ══════════════════════════════════════════════════════════════
       GOOGLE SIGN-IN — send Google token through Firebase first
       ══════════════════════════════════════════════════════════════ */

    fun googleLogin(googleIdToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Exchange Google ID token for Firebase ID token
                val firebaseToken = firebase.signInWithGoogle(googleIdToken)
                // Send Firebase token to our backend
                val response = api.googleLogin(GoogleAuthRequest(firebaseToken))
                val data = response.data
                if (data != null) {
                    saveSession(data.accessToken, data.refreshToken, data.user.id, data.user.name, data.user.phone, data.user.role)
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = response.message ?: "Google sign-in failed") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Google sign-in failed") }
            }
        }
    }

    /* ══════════════════════════════════════════════════════════════
       GUEST SESSION
       ══════════════════════════════════════════════════════════════ */

    fun continueAsGuest() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { api.guestLogin(GuestLoginRequest()) }
                .onSuccess { response ->
                    val data = response.data
                    if (data != null) {
                        saveSession(data.accessToken, data.refreshToken, data.user.id, data.user.name, data.user.phone, data.user.role, isGuest = true)
                        _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = response.message ?: "Guest login failed") }
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Could not connect") }
                }
        }
    }

    /* ══════════════════════════════════════════════════════════════
       REFRESH / RESEND
       ══════════════════════════════════════════════════════════════ */

    fun refreshToken(token: String) {
        viewModelScope.launch {
            runCatching { api.refreshToken(com.kiladarbar.data.remote.dto.RefreshTokenRequest(token)) }
                .onSuccess { response ->
                    response.data?.let {
                        saveSession(it.accessToken, it.refreshToken, it.user.id, it.user.name, it.user.phone, it.user.role)
                        _uiState.update { s -> s.copy(isLoggedIn = true) }
                    }
                }
        }
    }

    fun clearError()              = _uiState.update { it.copy(error = null) }
    fun setError(msg: String)     = _uiState.update { it.copy(error = msg, isLoading = false) }
    fun resetOtpSent()            = _uiState.update { it.copy(otpSent = false, verificationId = null) }

    private fun startResendTimer() {
        resendTimerJob?.cancel()
        resendTimerJob = viewModelScope.launch {
            for (i in 60 downTo 0) {
                _uiState.update { it.copy(resendSecondsLeft = i) }
                if (i > 0) delay(1000)
            }
        }
    }

    private suspend fun saveSession(
        accessToken: String, refreshToken: String,
        userId: String, userName: String?, phone: String?, role: String,
        isGuest: Boolean = false,
    ) {
        sessionManager.saveSession(
            accessToken  = accessToken,
            refreshToken = refreshToken,
            userId       = userId,
            userName     = userName,
            phone        = phone,
            role         = role,
            isGuest      = isGuest,
        )
    }

    override fun onCleared() {
        super.onCleared()
        resendTimerJob?.cancel()
    }
}
