package com.example.locationpins.ui.screen.map

import com.example.locationpins.data.remote.dto.pins.PinDto
import com.mapbox.maps.Style
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.geojson.Point

data class MapUiState(
    val currentStyleUri: String = Style.MAPBOX_STREETS,
    val query: String = "",
    val suggestions: List<SearchSuggestion> = emptyList(),
    val isSearching: Boolean = false,
    val showBottomSheet: Boolean = false,
    // cái này không phải là địa chỉ camera mọi lúc. mà nó chỉ đại diện cho địa chỉ sau khi chọn 1 địa điểm lúc search thôi.
    val cameraCoordinate: Point? = null,
    // list pin mà người dùng không sở hữu, nhưng nằm trong bán kính
    val greenPinList: List<PinDto> =  emptyList(),
    // list pin mà người dùng sở hữu
    val redPinList: List<PinDto> =  emptyList(),
    // vị trí hiện tại của user (từ GPS / Mapbox)
    val userLocation: Point = Point.fromLngLat(106.7, 10.77),
    val selectedPin: PinDto? = null
)
