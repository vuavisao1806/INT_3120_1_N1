package com.example.locationpins.ui.screen.map

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchSelectionCallback
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
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
            center(Point.fromLngLat(106.660172, 10.762622))
            pitch(0.0)
        }
    }

    // Khi ViewModel set cameraCoordinate, UI sẽ move camera ở đây
    LaunchedEffect(uiState.cameraCoordinate) {
        val coord = uiState.cameraCoordinate ?: return@LaunchedEffect
        mapViewportState.setCameraOptions {
            center(coord)
            zoom(10.0)
            pitch(0.0)
        }
        // Báo ngược lại cho ViewModel biết camera đã move
        viewModel.onCameraMoved()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        MapboxMap(
            modifier = Modifier.matchParentSize(),
            mapViewportState = mapViewportState,
        ) {
            // ========== UI của MAP ==========
            // ========== Khởi tạo location component chỉ 1 lần ==========
            MapEffect(Unit) { mapView ->
                mapView.location.updateSettings {
                    enabled = true
                    locationPuck = createDefault2DPuck(withBearing = true)
                    puckBearingEnabled = true
                    puckBearing = PuckBearing.COURSE
                }
            }

            // ========== Load style riêng biệt, không ảnh hưởng location ==========
            MapEffect(uiState.currentStyleUri) { mapView ->
                mapView.getMapboxMap().loadStyleUri(uiState.currentStyleUri)
            }
        }
        // ========== SEARCH BOX + SUGGESTIONS ==========
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            MapSearchBar(
                query = uiState.query,
                isSearching = uiState.isSearching,
                onQueryChange = { viewModel.onQueryChange(it) },
                onClear = { viewModel.onClearQuery() }
            )
            if (uiState.suggestions.isNotEmpty() && !uiState.isSearching) {
                SuggestionList(
                    suggestions = uiState.suggestions,
                    onClickSuggestion = { suggestion ->
                        viewModel.onSuggestionSelected(suggestion)
                    }
                )
            }
        }
        // ========== UI của 2 nút: nút follow về user và nút style map ==========
        MapControls(
            onClickStyle = { viewModel.onShowBottomSheet() },
            onClickMyLocation = { followUser(mapViewportState) }
        )
        // ========== UI của BottomSheet, nó sẽ hiện lên để người dùng lựa chọn khi nhấn váo nút style map ==========
        if (uiState.showBottomSheet) {
            MapStyleBottomSheet(
                currentStyleUri = uiState.currentStyleUri,
                onDismiss = { viewModel.onHideBottomSheet() },
                onStyleSelected = { styleUri ->
                    viewModel.onMapStyleSelected(styleUri)
                    viewModel.onHideBottomSheet()
                }
            )
        }
        // ========== UI việc search ==========
    }
}

@Composable
private fun MapSearchBar(
    query: String,
    isSearching: Boolean,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit
) {
    Column {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(28.dp))
                .background(Color.White, RoundedCornerShape(28.dp)),
            placeholder = {
                Text("Tìm kiếm địa điểm...", color = Color.Gray)
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.Gray
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = Color.Gray
                        )
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            shape = RoundedCornerShape(28.dp)
        )

        if (isSearching) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .shadow(2.dp, RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Đang tìm kiếm...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestionList(
    suggestions: List<SearchSuggestion>,
    onClickSuggestion: (SearchSuggestion) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp)
        ) {
            items(suggestions) { suggestion ->
                SuggestionItem(
                    suggestion = suggestion,
                    onClick = { onClickSuggestion(suggestion) }
                )
                if (suggestion != suggestions.last()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color.LightGray.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestionItem(
    suggestion: SearchSuggestion,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(24.dp)
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = suggestion.name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.Black
            )

            val addressText = suggestion.fullAddress
                ?: suggestion.descriptionText
                ?: suggestion.address?.formattedAddress()
                ?: ""

            if (addressText.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = addressText,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.Gray
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapStyleBottomSheet(
    currentStyleUri: String,
    onDismiss: () -> Unit,
    onStyleSelected: (String) -> Unit
) {
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