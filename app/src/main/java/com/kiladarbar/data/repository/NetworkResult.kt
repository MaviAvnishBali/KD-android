package com.kiladarbar.data.repository

/**
 * Sealed result wrapper for network calls.
 * Use in ViewModels to drive UI state.
 */
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String, val code: Int? = null) : NetworkResult<Nothing>()
    data object Loading : NetworkResult<Nothing>()

    val isSuccess get() = this is Success
    val isError   get() = this is Error
    val isLoading get() = this is Loading

    fun getOrNull(): T? = if (this is Success) data else null
    fun errorMessage(): String? = if (this is Error) message else null
}

/**
 * Safe network call wrapper — catches exceptions and maps to NetworkResult.
 */
suspend fun <T> safeApiCall(call: suspend () -> com.kiladarbar.data.remote.dto.ApiResponse<T>): NetworkResult<T> {
    return try {
        val response = call()
        if (response.success && response.data != null) {
            NetworkResult.Success(response.data)
        } else {
            NetworkResult.Error(response.message ?: "Request failed")
        }
    } catch (e: retrofit2.HttpException) {
        val code = e.code()
        val msg  = when (code) {
            401 -> "Session expired. Please log in again."
            403 -> "You don't have permission to perform this action."
            404 -> "Resource not found."
            422 -> "Validation failed. Check your input."
            429 -> "Too many requests. Please slow down."
            500 -> "Server error. Please try again later."
            else -> e.message ?: "Network error"
        }
        NetworkResult.Error(msg, code)
    } catch (e: java.net.SocketTimeoutException) {
        NetworkResult.Error("Request timed out. Check your connection.")
    } catch (e: java.net.UnknownHostException) {
        NetworkResult.Error("No internet connection.")
    } catch (e: Exception) {
        NetworkResult.Error(e.message ?: "An unexpected error occurred.")
    }
}
