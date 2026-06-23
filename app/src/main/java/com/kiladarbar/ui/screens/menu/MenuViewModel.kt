package com.kiladarbar.ui.screens.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiladarbar.data.repository.CartRepository
import com.kiladarbar.data.repository.MenuRepository
import com.kiladarbar.data.repository.NetworkResult
import com.kiladarbar.ui.models.UiCategory
import com.kiladarbar.ui.models.UiMenuItem
import com.kiladarbar.ui.models.toUiCategory
import com.kiladarbar.ui.models.toUiMenuItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MenuUiState(
    val isLoadingCategories: Boolean          = true,
    val isLoadingItems:      Boolean          = true,
    val categories:          List<UiCategory> = emptyList(),
    val items:               List<UiMenuItem> = emptyList(),
    val selectedCategoryId:  Int              = 0,
    val searchQuery:         String           = "",
    val cartQuantities:      Map<String, Int> = emptyMap(),
    val error:               String?          = null,
)

@OptIn(FlowPreview::class)
@HiltViewModel
class MenuViewModel @Inject constructor(
    private val menuRepository: MenuRepository,
    private val cartRepository: CartRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MenuUiState())
    val uiState = _uiState.asStateFlow()

    private val searchFlow = MutableStateFlow("")

    init {
        loadCategories()
        loadItems()

        viewModelScope.launch {
            searchFlow
                .debounce(350)
                .distinctUntilChanged()
                .collect { query ->
                    _uiState.update { it.copy(searchQuery = query) }
                    loadItems(
                        categoryId = _uiState.value.selectedCategoryId,
                        search     = query.ifBlank { null },
                    )
                }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            when (val r = menuRepository.getCategories()) {
                is NetworkResult.Success -> _uiState.update {
                    it.copy(isLoadingCategories = false, categories = r.data.map { c -> c.toUiCategory() })
                }
                is NetworkResult.Error -> _uiState.update {
                    it.copy(isLoadingCategories = false, error = r.message)
                }
                else -> {}
            }
        }
    }

    fun loadItems(categoryId: Int = _uiState.value.selectedCategoryId, search: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingItems = true, error = null) }
            val r = menuRepository.getMenuItems(
                categoryId = if (categoryId == 0) null else categoryId,
                search     = search,
            )
            when (r) {
                is NetworkResult.Success -> _uiState.update {
                    it.copy(isLoadingItems = false, items = r.data.content.map { dto -> dto.toUiMenuItem() })
                }
                is NetworkResult.Error -> _uiState.update {
                    it.copy(isLoadingItems = false, error = r.message)
                }
                else -> {}
            }
        }
    }

    fun selectCategory(categoryId: Int) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
        loadItems(categoryId = categoryId, search = _uiState.value.searchQuery.ifBlank { null })
    }

    fun onSearchChange(query: String) { searchFlow.value = query }

    fun addToCart(item: UiMenuItem) {
        _uiState.update { s ->
            s.copy(cartQuantities = s.cartQuantities + (item.id to (s.cartQuantities[item.id] ?: 0) + 1))
        }
        viewModelScope.launch { cartRepository.addItem(item.id) }
    }

    fun decrementCart(item: UiMenuItem) {
        val newQty = (_uiState.value.cartQuantities[item.id] ?: 1) - 1
        _uiState.update { s ->
            s.copy(cartQuantities = if (newQty <= 0) s.cartQuantities - item.id else s.cartQuantities + (item.id to newQty))
        }
        viewModelScope.launch {
            if (newQty <= 0) cartRepository.removeItem(item.id)
            else cartRepository.updateItem(item.id, newQty)
        }
    }
}
