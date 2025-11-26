package com.example.locationpins.ui.screen.map

import com.mapbox.maps.Style

data class MapUiState(
    val showBottomSheet: Boolean = false,
    val currentStyleUri: String = Style.MAPBOX_STREETS
)
