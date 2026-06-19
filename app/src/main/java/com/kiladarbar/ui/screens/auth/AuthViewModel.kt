package com.kiladarbar.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiladarbar.data.local.SessionManager
import com.kiladarbar.data.remote.ApiService
import com.kiladarbar.data.remote.dto.GoogleAuthRequest
import com.kiladarbar.data.remote.dto.SendOtpRequest
import com.kiladarbar.data.remote.dto.VerifyOtpRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val otpSent: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null,
    val resendSecondsLeft: Int = 0,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val api: ApiService,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    private var resendTimerJob: Job? = null

    fun sendOtp(phone: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { api.sendOtp(SendOtpRequest(phone)) }
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, otpSent = true) }
                    startResendTimer()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to send OTP") }
                }
        }
    }

    fun verifyOtp(phone: String, otp: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { api.verifyOtp(VerifyOtpRequest(phone, otp)) }
                .onSuccess { response ->
                    val data = response.data
                    if (data != null) {
                        sessionManager.saveSession(
                            accessToken  = data.accessToken,
                            refreshToken = data.refreshToken,
                            userId       = data.user.id,
                            userName     = data.user.name,
                            phone        = data.user.phone,
                            role         = data.user.role,
                        )
                        _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = response.message ?: "Verification failed") }
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Wrong OTP, please try again") }
                }
        }
    }

    fun googleLogin(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { api.googleLogin(GoogleAuthRequest(idToken)) }
                .onSuccess { response ->
                    val data = response.data
                    if (data != null) {
                        sessionManager.saveSession(
                            accessToken  = data.accessToken,
                            refreshToken = data.refreshToken,
                            userId       = data.user.id,
                            userName     = data.user.name,
                            phone        = data.user.phone,
                            role         = data.user.role,
                        )
                        _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = response.message ?: "Google sign-in failed") }
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Google sign-in failed") }
                }
        }
    }

    fun resendOtp(phone: String) {
        if (_uiState.value.resendSecondsLeft > 0) return
        sendOtp(phone)
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    private fun startResendTimer() {
        resendTimerJob?.cancel()
        resendTimerJob = viewModelScope.launch {
            for (i in 60 downTo 0) {
                _uiState.update { it.copy(resendSecondsLeft = i) }
                if (i > 0) delay(1000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        resendTimerJob?.cancel()
    }
}
