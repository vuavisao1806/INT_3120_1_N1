package com.example.locationpins.ui.screen.map

import com.mapbox.maps.Style

data class MapStyle(
    val id: String,
    val name: String,
    val description: String,
    val styleUri: String
)

// Danh sách các Map Styles
val availableMapStyles = listOf(
    MapStyle(
        id = "streets",
        name = "Streets",
        description = "Bản đồ đường phố chi tiết",
        styleUri = Style.MAPBOX_STREETS
    ),
    MapStyle(
        id = "outdoors",
        name = "Outdoors",
        description = "Phù hợp cho hoạt động ngoài trời",
        styleUri = Style.OUTDOORS
    ),
    MapStyle(
        id = "light",
        name = "Light",
        description = "Phong cách sáng tối giản",
        styleUri = Style.LIGHT
    ),
    MapStyle(
        id = "dark",
        name = "Dark",
        description = "Phong cách tối cho ban đêm",
        styleUri = Style.DARK
    ),
    MapStyle(
        id = "satellite",
        name = "Satellite",
        description = "Hình ảnh vệ tinh",
        styleUri = Style.SATELLITE
    ),
    MapStyle(
        id = "satellite_streets",
        name = "Satellite Streets",
        description = "Vệ tinh kèm nhãn đường",
        styleUri = Style.SATELLITE_STREETS
    )
)