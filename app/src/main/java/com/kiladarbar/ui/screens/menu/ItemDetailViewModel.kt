package com.kiladarbar.ui.screens.menu

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiladarbar.data.repository.CartRepository
import com.kiladarbar.data.repository.MenuRepository
import com.kiladarbar.data.repository.NetworkResult
import com.kiladarbar.ui.models.UiMenuItem
import com.kiladarbar.ui.models.toUiMenuItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ItemDetailUiState(
    val item:      UiMenuItem? = null,
    val qty:       Int         = 0,
    val isLoading: Boolean     = true,
    val error:     String?     = null,
)

@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    private val menuRepository: MenuRepository,
    private val cartRepository: CartRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val itemId: String = checkNotNull(savedStateHandle["itemId"])

    private val _uiState = MutableStateFlow(ItemDetailUiState())
    val uiState = _uiState.asStateFlow()

    init { loadItem() }

    private fun loadItem() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val r = menuRepository.getMenuItem(itemId)) {
                is NetworkResult.Success -> _uiState.update {
                    it.copy(isLoading = false, item = r.data.toUiMenuItem())
                }
                is NetworkResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = r.message)
                }
                else -> {}
            }
        }
    }

    fun addToCart() {
        _uiState.update { it.copy(qty = it.qty + 1) }
        viewModelScope.launch { cartRepository.addItem(itemId, 1) }
    }

    fun increment() {
        _uiState.update { it.copy(qty = it.qty + 1) }
        viewModelScope.launch { cartRepository.addItem(itemId, 1) }
    }

    fun decrement() {
        val newQty = (_uiState.value.qty - 1).coerceAtLeast(0)
        _uiState.update { it.copy(qty = newQty) }
        viewModelScope.launch {
            if (newQty == 0) cartRepository.removeItem(itemId)
            else cartRepository.updateItem(itemId, newQty)
        }
    }
}
