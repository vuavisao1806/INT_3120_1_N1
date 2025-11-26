package com.example.locationpins.ui.screen.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions
import androidx.compose.runtime.LaunchedEffect
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

@Composable
fun MapScreen() {
    // Xin quyền location
    RequestLocationPermission()

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
            MapEffect(Unit) { mapView ->
                // Load style cơ bản
                mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) {

                    // Bật chấm xanh vị trí user
                    mapView.location.updateSettings {
                        enabled = true
                        locationPuck = createDefault2DPuck(withBearing = true)
                        puckBearingEnabled = true
                        puckBearing = PuckBearing.COURSE
                    }

                    // Cho camera follow vị trí user
                    followUser(mapViewportState)
                }
            }
        }
    }
}


// Hàm này phục vụ xin quyền
@Composable
private fun RequestLocationPermission() {
    val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        // Nếu cần thì xử lí result ở đây
    }

    LaunchedEffect(Unit) {
        launcher.launch(permissions)
    }
}

// Hàm này phục vụ việc camera sẽ theo dõi đến địa chỉ gps người dùng
private fun followUser(mapViewportState: MapViewportState) {
    val followOptions = FollowPuckViewportStateOptions
        .Builder()
        .pitch(0.0)
        .build()

    mapViewportState.transitionToFollowPuckState(followOptions)
}
