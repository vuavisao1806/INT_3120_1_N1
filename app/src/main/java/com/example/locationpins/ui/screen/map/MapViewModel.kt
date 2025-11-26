package com.example.locationpins.ui.screen.map


import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MapViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState

    fun onShowBottomSheet() {
        _uiState.value = _uiState.value.copy(showBottomSheet = true)
    }

    fun onHideBottomSheet() {
        _uiState.value = _uiState.value.copy(showBottomSheet = false)
    }

    fun onMapStyleSelected(styleUri: String) {
        _uiState.value = _uiState.value.copy(currentStyleUri = styleUri)
    }
}

