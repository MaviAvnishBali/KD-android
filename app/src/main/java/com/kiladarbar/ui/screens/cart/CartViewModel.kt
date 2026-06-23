package com.kiladarbar.ui.screens.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiladarbar.data.local.SessionManager
import com.kiladarbar.data.remote.dto.CartDto
import com.kiladarbar.data.repository.CartRepository
import com.kiladarbar.data.repository.MenuRepository
import com.kiladarbar.data.repository.NetworkResult
import com.kiladarbar.ui.models.UiMenuItem
import com.kiladarbar.ui.models.toUiMenuItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartUiState(
    val isLoading:       Boolean          = true,
    val isGuest:         Boolean          = false,
    val cart:            CartDto?         = null,
    val suggestedItems:  List<UiMenuItem> = emptyList(),
    val error:           String?          = null,   // only set for non-auth errors on logged-in users
    val showLoginDialog: Boolean          = false,
)

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val menuRepository: MenuRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val isGuest         = sessionManager.isGuest()
            val cartDeferred    = async { cartRepository.getCart() }
            val suggestDeferred = async { menuRepository.getBestSellers() }

            val cartResult  = cartDeferred.await()
            val suggestResult = suggestDeferred.await()

            // --- Cart data resolution ---
            // For guests: 401/403 just means "no cart yet" — treat as empty, not as an error.
            // For logged-in users: surface real errors so Retry is shown.
            val cartData: CartDto? = when (cartResult) {
                is NetworkResult.Success -> cartResult.data
                is NetworkResult.Error   -> {
                    val isAuthError = cartResult.code == 401 || cartResult.code == 403
                    if (isGuest || isAuthError) {
                        null   // treat as empty cart
                    } else {
                        _uiState.update {
                            it.copy(isLoading = false, isGuest = isGuest, error = cartResult.message)
                        }
                        // Still load suggestions even on cart error
                        loadSuggestions(suggestResult, emptySet(), isGuest)
                        return@launch
                    }
                }
                else -> null
            }

            loadSuggestions(suggestResult, cartData?.items?.map { it.menuItemId }?.toSet() ?: emptySet(), isGuest)

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    isGuest   = isGuest,
                    cart      = cartData,
                    error     = null,
                )
            }
        }
    }

    private fun loadSuggestions(
        result:     NetworkResult<List<com.kiladarbar.data.remote.dto.MenuItemDto>>,
        inCartIds:  Set<String>,
        isGuest:    Boolean,
    ) {
        val items = if (result is NetworkResult.Success)
            result.data.map { it.toUiMenuItem() }.filterNot { it.id in inCartIds }.take(5)
        else emptyList()

        _uiState.update { it.copy(suggestedItems = items, isGuest = isGuest) }
    }

    fun updateItem(itemId: String, qty: Int) {
        viewModelScope.launch {
            val result = if (qty <= 0) cartRepository.removeItem(itemId)
                         else cartRepository.updateItem(itemId, qty)
            if (result is NetworkResult.Success) {
                _uiState.update { it.copy(cart = result.data) }
            }
        }
    }

    fun addSuggestedItem(menuItemId: String) {
        viewModelScope.launch {
            // If guest with no valid session, just show login dialog
            if (_uiState.value.isGuest && !sessionManager.isLoggedIn()) {
                _uiState.update { it.copy(showLoginDialog = true) }
                return@launch
            }
            val result = cartRepository.addItem(menuItemId, 1)
            if (result is NetworkResult.Success) {
                _uiState.update { state ->
                    state.copy(
                        cart           = result.data,
                        suggestedItems = state.suggestedItems.filterNot { it.id == menuItemId },
                    )
                }
            }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            cartRepository.clearCart()
            _uiState.update { it.copy(cart = null) }
        }
    }

    /** Called whenever the guest tries to do anything that requires a real account. */
    fun requestCheckout(onProceed: () -> Unit) {
        if (_uiState.value.isGuest) {
            _uiState.update { it.copy(showLoginDialog = true) }
        } else {
            onProceed()
        }
    }

    fun showLoginDialog()    = _uiState.update { it.copy(showLoginDialog = true)  }
    fun dismissLoginDialog() = _uiState.update { it.copy(showLoginDialog = false) }
}
