package com.kiladarbar.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiladarbar.data.local.SessionManager
import com.kiladarbar.data.repository.CartRepository
import com.kiladarbar.data.repository.MenuRepository
import com.kiladarbar.data.repository.NetworkResult
import com.kiladarbar.ui.models.UiBanner
import com.kiladarbar.ui.models.UiCategory
import com.kiladarbar.ui.models.UiMenuItem
import com.kiladarbar.ui.models.UiOffer
import com.kiladarbar.ui.models.toUiBanner
import com.kiladarbar.ui.models.toUiCategory
import com.kiladarbar.ui.models.toUiMenuItem
import com.kiladarbar.ui.models.toUiOffer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading:      Boolean          = true,
    val userName:       String?          = null,
    val isLoggedIn:     Boolean          = false,
    val loyaltyPoints:  Int              = 0,
    val loyaltyTier:    String           = "Silver",
    val cartCount:      Int              = 0,
    val cartQuantities: Map<String, Int> = emptyMap(),
    val banners:        List<UiBanner>   = emptyList(),
    val offers:         List<UiOffer>    = emptyList(),
    val categories:     List<UiCategory> = emptyList(),
    val bestSellers:    List<UiMenuItem> = emptyList(),
    val recommended:    List<UiMenuItem> = emptyList(),
    val error:          String?          = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val menuRepository: MenuRepository,
    private val cartRepository: CartRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    val userName = sessionManager.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init { loadHome() }

    fun loadHome() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val bannersDeferred     = async { menuRepository.getBanners() }
            val offersDeferred      = async { menuRepository.getOffers() }
            val categoriesDeferred  = async { menuRepository.getCategories() }
            val bestSellersDeferred = async { menuRepository.getBestSellers() }
            val recommendedDeferred = async { menuRepository.getRecommended() }

            val banners     = bannersDeferred.await()
            val offers      = offersDeferred.await()
            val categories  = categoriesDeferred.await()
            val bestSellers = bestSellersDeferred.await()
            val recommended = recommendedDeferred.await()

            _uiState.update { state ->
                state.copy(
                    isLoading   = false,
                    banners     = if (banners     is NetworkResult.Success) banners.data.map     { it.toUiBanner()   } else state.banners,
                    offers      = if (offers      is NetworkResult.Success) offers.data.map      { it.toUiOffer()    } else state.offers,
                    categories  = if (categories  is NetworkResult.Success) categories.data.map  { it.toUiCategory() } else state.categories,
                    bestSellers = if (bestSellers  is NetworkResult.Success) bestSellers.data.map { it.toUiMenuItem() } else state.bestSellers,
                    recommended = if (recommended  is NetworkResult.Success) recommended.data.map { it.toUiMenuItem() } else state.recommended,
                    error = when {
                        bestSellers is NetworkResult.Error -> bestSellers.message
                        categories  is NetworkResult.Error -> categories.message
                        else -> null
                    },
                )
            }
        }
    }

    fun addToCart(item: UiMenuItem) {
        _uiState.update { state ->
            val qty = (state.cartQuantities[item.id] ?: 0) + 1
            state.copy(
                cartQuantities = state.cartQuantities + (item.id to qty),
                cartCount      = state.cartCount + 1,
            )
        }
        viewModelScope.launch { cartRepository.addItem(item.id) }
    }

    fun updateCartQuantity(itemId: String, quantity: Int) {
        _uiState.update { state ->
            val old  = state.cartQuantities[itemId] ?: 0
            val diff = quantity - old
            val newQty = if (quantity <= 0) state.cartQuantities - itemId
                         else state.cartQuantities + (itemId to quantity)
            state.copy(
                cartQuantities = newQty,
                cartCount      = maxOf(0, state.cartCount + diff),
            )
        }
        viewModelScope.launch {
            if (quantity <= 0) cartRepository.removeItem(itemId)
            else cartRepository.updateItem(itemId, quantity)
        }
    }
}
