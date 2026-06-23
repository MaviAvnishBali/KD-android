package com.kiladarbar.ui.screens.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiladarbar.data.remote.ApiService
import com.kiladarbar.domain.model.DriverInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrderTrackingUiState(
    val orderNumber:      String?     = null,
    val orderTime:        String?     = null,
    val status:           String?     = null,
    val estimatedMinutes: Int?        = null,
    val driverInfo:       DriverInfo? = null,
    val items:            List<Any>?  = null,
    val subtotal:         Double?     = null,
    val deliveryCharge:   Double?     = null,
    val gst:              Double?     = null,
    val discount:         Double?     = null,
    val total:            Double?     = null,
    val isLoading:        Boolean     = false,
)

@HiltViewModel
class OrderTrackingViewModel @Inject constructor(
    private val api: ApiService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderTrackingUiState())
    val uiState = _uiState.asStateFlow()

    private var trackingJob: Job? = null

    fun loadOrder(orderId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { api.getOrder(orderId) }
                .onSuccess { resp ->
                    val dto = resp.data ?: return@onSuccess
                    _uiState.update {
                        it.copy(
                            isLoading        = false,
                            orderNumber      = dto.orderNumber,
                            orderTime        = dto.createdAt,
                            status           = dto.status,
                            estimatedMinutes = dto.estimatedMinutes,
                            driverInfo       = dto.deliveryPartner?.let { d ->
                                DriverInfo(
                                    name          = d.name,
                                    phone         = d.phone,
                                    vehicleNumber = d.vehicleNumber,
                                    rating        = d.rating.toFloat(),
                                )
                            },
                            subtotal         = dto.subtotal,
                            deliveryCharge   = dto.deliveryCharge,
                            gst              = dto.cgstAmount + dto.sgstAmount,
                            discount         = dto.discountAmount,
                            total            = dto.totalAmount,
                        )
                    }
                }
                .onFailure { _uiState.update { it.copy(isLoading = false) } }
        }
    }

    fun startTracking(orderId: String) {
        trackingJob = viewModelScope.launch {
            while (true) {
                delay(15_000)
                runCatching { api.getOrder(orderId) }.onSuccess { resp ->
                    resp.data?.let { dto ->
                        _uiState.update {
                            it.copy(status = dto.status, estimatedMinutes = dto.estimatedMinutes)
                        }
                    }
                }
            }
        }
    }

    fun stopTracking() { trackingJob?.cancel() }

    fun cancelOrder() {
        val orderId = _uiState.value.orderNumber ?: return
        viewModelScope.launch {
            runCatching { api.cancelOrder(orderId) }
                .onSuccess { _uiState.update { it.copy(status = "CANCELLED") } }
        }
    }
}
