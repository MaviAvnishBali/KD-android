package com.kiladarbar.ui.screens.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiladarbar.data.repository.NetworkResult
import com.kiladarbar.data.repository.OrderRepository
import com.kiladarbar.ui.models.UiOrder
import com.kiladarbar.ui.models.toUiOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrdersUiState(
    val isLoading:   Boolean    = true,
    val orders:      List<UiOrder> = emptyList(),
    val error:       String?    = null,
    val hasMore:     Boolean    = false,
)

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState = _uiState.asStateFlow()

    init { loadOrders() }

    fun loadOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val r = orderRepository.getOrders()) {
                is NetworkResult.Success -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        orders    = r.data.content.map { dto -> dto.toUiOrder() },
                        hasMore   = !r.data.last,
                    )
                }
                is NetworkResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = r.message)
                }
                else -> {}
            }
        }
    }
}
