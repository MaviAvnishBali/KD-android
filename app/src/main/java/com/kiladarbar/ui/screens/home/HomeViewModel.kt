package com.kiladarbar.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiladarbar.data.remote.ApiService
import com.kiladarbar.domain.model.MenuItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val userName: String? = null,
    val cartCount: Int = 0,
    val activeOffers: List<Any> = emptyList(),
    val categories: List<Any> = emptyList(),
    val bestSellers: List<MenuItem> = emptyList(),
    val recommended: List<MenuItem> = emptyList(),
    val isLoggedIn: Boolean = false,
    val loyaltyPoints: Int = 0,
    val loyaltyTier: String = "Silver",
    val cartQuantities: Map<String, Int> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val api: ApiService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init { loadHome() }

    private fun loadHome() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                val bestSellers = api.getBestSellers().data?.map { dto ->
                    MenuItem(dto.id, dto.name, dto.description, dto.price, dto.imageUrl, dto.isVeg, dto.category, dto.rating)
                } ?: emptyList()
                val recommended = api.getRecommended().data?.map { dto ->
                    MenuItem(dto.id, dto.name, dto.description, dto.price, dto.imageUrl, dto.isVeg, dto.category, dto.rating)
                } ?: emptyList()
                bestSellers to recommended
            }.onSuccess { (best, rec) ->
                _uiState.update { it.copy(isLoading = false, bestSellers = best, recommended = rec) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun addToCart(item: MenuItem) {
        _uiState.update { state ->
            val current = state.cartQuantities[item.id] ?: 0
            state.copy(
                cartQuantities = state.cartQuantities + (item.id to current + 1),
                cartCount = state.cartCount + 1,
            )
        }
    }

    fun updateCartQuantity(item: MenuItem, quantity: Int) {
        _uiState.update { state ->
            val diff = quantity - (state.cartQuantities[item.id] ?: 0)
            val newQty = if (quantity <= 0) state.cartQuantities - item.id
                         else state.cartQuantities + (item.id to quantity)
            state.copy(cartQuantities = newQty, cartCount = maxOf(0, state.cartCount + diff))
        }
    }
}
