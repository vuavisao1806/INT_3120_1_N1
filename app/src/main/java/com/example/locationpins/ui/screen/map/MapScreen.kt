package com.example.locationpins.ui.screen.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState

@Composable
fun MapScreen() {
    // Trạng thái camera ban đầu
    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(14.0)
            // Toạ độ ví dụ: TP.HCM
            center(Point.fromLngLat(106.660172, 10.762622))
            pitch(0.0)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapViewportState = mapViewportState
        ) {
            // Load style cơ bản
            MapEffect(Unit) { mapView ->
                mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)
            }
        }
    }
}
