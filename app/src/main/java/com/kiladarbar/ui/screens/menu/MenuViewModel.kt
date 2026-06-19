package com.kiladarbar.ui.screens.menu

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class MenuUiState(
    val isLoading: Boolean = false,
)

@HiltViewModel
class MenuViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(MenuUiState())
    val uiState = _uiState.asStateFlow()
}
