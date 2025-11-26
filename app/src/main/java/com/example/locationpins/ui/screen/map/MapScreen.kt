package com.example.locationpins.ui.screen.map

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen() {
    val viewModel: MapViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
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
            modifier = Modifier.matchParentSize(),
            mapViewportState = mapViewportState,
        ) {
            MapEffect(uiState.currentStyleUri) { mapView ->
                mapView.getMapboxMap().loadStyleUri(uiState.currentStyleUri) {
                    mapView.location.updateSettings {
                        enabled = true
                        locationPuck = createDefault2DPuck(withBearing = true)
                        puckBearingEnabled = true
                        puckBearing = PuckBearing.COURSE
                    }
                    followUser(mapViewportState)
                }
            }
        }
        MapControls(
            onClickStyle = { viewModel.onShowBottomSheet() },
            onClickMyLocation = { followUser(mapViewportState) }
        )

        MapStyleBottomSheet(
            visible = uiState.showBottomSheet,
            currentStyleUri = uiState.currentStyleUri,
            onDismiss = { viewModel.onHideBottomSheet() },
            onStyleSelected = { styleUri ->
                viewModel.onMapStyleSelected(styleUri)
                viewModel.onHideBottomSheet()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapStyleBottomSheet(
    visible: Boolean,
    currentStyleUri: String,
    onDismiss: () -> Unit,
    onStyleSelected: (String) -> Unit
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = {
            coroutineScope.launch {
                sheetState.hide()
                onDismiss()
            }
        },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Chọn kiểu bản đồ",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )

            LazyColumn {
                items(availableMapStyles) { style ->
                    MapStyleItem(
                        mapStyle = style,
                        isSelected = currentStyleUri == style.styleUri,
                        onClick = {
                            coroutineScope.launch {
                                sheetState.hide()
                                onStyleSelected(style.styleUri)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BoxScope.MapControls(
    onClickStyle: () -> Unit,
    onClickMyLocation: () -> Unit
) {
    Column(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FloatingActionButton(
            onClick = onClickStyle,
            containerColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Layers,
                contentDescription = "Chọn kiểu bản đồ",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        FloatingActionButton(
            onClick = onClickMyLocation,
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Vị trí của tôi",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun MapStyleItem(
    mapStyle: MapStyle,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else Color.Transparent
            )
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = mapStyle.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = mapStyle.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
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
