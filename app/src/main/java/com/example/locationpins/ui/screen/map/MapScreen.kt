package com.example.locationpins.ui.screen.map

import android.Manifest
import android.graphics.BitmapFactory
import android.util.Log
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
import androidx.compose.material.icons.filled.Explore
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.locationpins.R
import com.example.locationpins.data.model.Post
import com.example.locationpins.data.remote.dto.pins.PinDto
import com.example.locationpins.ui.screen.pinDiscovery.PinDiscoveryScreen
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.RenderedQueryGeometry
import com.mapbox.maps.RenderedQueryOptions
import com.mapbox.maps.extension.compose.DisposableMapEffect
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.fillLayer
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor
import com.mapbox.maps.extension.style.layers.properties.generated.TextJustify
import com.mapbox.maps.extension.style.layers.properties.generated.TextTranslateAnchor
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.getSource
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfTransformation
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onPinPress: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val viewModel: MapViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showDiscoveryGame by remember { mutableStateOf(false) }

    // 1) init LocationManager đúng 1 lần
    LaunchedEffect(Unit) {
        LocationManager.init(context)
    }


    // 2) Xin quyền + nếu granted => báo cho LocationManager bắt đầu update
    RequestLocationPermission(
        onGranted = { LocationManager.onPermissionGranted() },
        onDenied = { /* optional: show message */ }
    )

    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(14.0)
            center(Point.fromLngLat(105.0, 21.0))
            pitch(0.0)
        }
    }

    // Khi ViewModel set cameraCoordinate (search chọn địa điểm), UI move camera
    LaunchedEffect(uiState.cameraCoordinate) {
        val coord = uiState.cameraCoordinate ?: return@LaunchedEffect
        mapViewportState.setCameraOptions {
            center(coord)
            zoom(10.0)
            pitch(0.0)
        }
        viewModel.onCameraMoved()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MapboxMap(
            modifier = Modifier.matchParentSize(),
            mapViewportState = mapViewportState,
        ) {
            // CHỈ BẬT PUCK XANH (KHÔNG LISTENER)
            DisposableMapEffect(Unit) { mapView ->
                val locationComponent = mapView.location
                locationComponent.updateSettings {
                    enabled = true
                    locationPuck = createDefault2DPuck(withBearing = true)
                    puckBearingEnabled = true
                    puckBearing = PuckBearing.COURSE
                }
                onDispose { }
            }

            // ==========================================================
            // FIX BUG STYLE CHANGE: Tải Style, Allowed Area, và Clustering Layers
            // Khối MapEffect này chạy lại MỖI KHI Style, Vị trí hoặc List Pin thay đổi.
            // ==========================================================
            MapEffect(
                uiState.currentStyleUri,
                uiState.userLocation,
                uiState.redPinList,
                uiState.greenPinList
            ) { mapView ->
                val mapboxMap = mapView.getMapboxMap()
                val ctx = mapView.context

                // B1: Tải Style mới. Thêm toàn bộ logic layers vào callback { style -> ... }
                mapboxMap.loadStyleUri(uiState.currentStyleUri) { style ->

                    // --- B2: Thêm lại Vùng cho phép (Allowed Area) ---
                    val allowedCenter: Point? = uiState.userLocation
                    val allowedRadiusMeters = 100000.0

                    if (allowedCenter != null) {
                        val center = allowedCenter
                        val sourceId = "allowed-area-source"
                        val layerId = "allowed-area-layer"

                        val polygon: Polygon = TurfTransformation.circle(
                            center,
                            allowedRadiusMeters,
                            64,
                            TurfConstants.UNIT_METERS
                        )
                        val feature = Feature.fromGeometry(polygon)

                        // Thêm Source và Layer cho Allowed Area vào Style mới
                        style.addSource(
                            geoJsonSource(sourceId) { feature(feature) }
                        )
                        style.addLayer(
                            fillLayer(layerId, sourceId) {
                                fillColor("#5675FF")
                                fillOpacity(0.3)
                                fillOutlineColor("#5675FF")
                            }
                        )
                    }

                    // --- B3: Thêm lại logic Clustering và Pins ---
                    val redPins = uiState.redPinList
                    val greenPins = uiState.greenPinList

                    // Data -> FeatureCollection
                    val redFeatures = redPins.map { pin ->
                        Feature.fromGeometry(
                            Point.fromLngLat(pin.longitude, pin.latitude)
                        ).apply {
                            addStringProperty("type", "red")
                            addStringProperty("pinId", pin.pinId.toString())
                        }
                    }
                    val greenFeatures = greenPins.map { pin ->
                        Feature.fromGeometry(
                            Point.fromLngLat(pin.longitude, pin.latitude)
                        ).apply {
                            addStringProperty("type", "green")
                            addStringProperty("pinId", pin.pinId.toString())
                        }
                    }

                    val redFC = FeatureCollection.fromFeatures(redFeatures)
                    val greenFC = FeatureCollection.fromFeatures(greenFeatures)

                    // Icons (Cần thêm lại vào Style mới)
                    val redBitmap = BitmapFactory.decodeResource(ctx.resources, R.drawable.pin_red)
                    val redClusterBitmap = BitmapFactory.decodeResource(ctx.resources, R.drawable.pin_red_cluster)
                    val greenBitmap = BitmapFactory.decodeResource(ctx.resources, R.drawable.pin_green)
                    val greenClusterBitmap = BitmapFactory.decodeResource(ctx.resources, R.drawable.pin_green_cluster)

                    try { style.addImage("pin-red", redBitmap) } catch (_: Throwable) { runCatching { style.addImage("pin-red", redBitmap) } }
                    try { style.addImage("pin-red-cluster", redClusterBitmap) } catch (_: Throwable) { runCatching { style.addImage("pin-red-cluster", redClusterBitmap) } }
                    try { style.addImage("pin-green", greenBitmap) } catch (_: Throwable) { runCatching { style.addImage("pin-green", greenBitmap) } }
                    try { style.addImage("pin-green-cluster", greenClusterBitmap) } catch (_: Throwable) { runCatching { style.addImage("pin-green-cluster", greenClusterBitmap) } }


                    // RED source + layers
                    val redSourceId = "red-pins-source"
                    val redUnclusteredLayerId = "red-unclustered-layer"
                    val redClusterIconLayerId = "red-clusters-icon-layer"
                    val redClusterTextLayerId = "red-clusters-text-layer"

                    style.addSource(
                        geoJsonSource(redSourceId) {
                            featureCollection(redFC)
                            cluster(true)
                            clusterRadius(50)
                            clusterMaxZoom(50)
                            clusterMinPoints(2)
                        }
                    )
                    style.addLayer(symbolLayer(redClusterIconLayerId, redSourceId) { filter(Expression.has("point_count")); iconImage("pin-red-cluster"); iconAnchor(IconAnchor.BOTTOM); iconAllowOverlap(true); iconSize(0.08) })
                    style.addLayer(symbolLayer(redClusterTextLayerId, redSourceId) { filter(Expression.has("point_count")); textField(Expression.get("point_count_abbreviated")); textSize(12.0); textColor("#000000"); textHaloColor("#FFFFFF"); textHaloWidth(0.0); textAnchor(TextAnchor.CENTER); textJustify(TextJustify.CENTER); textTranslate(listOf(8.0, -36.8)); textTranslateAnchor(TextTranslateAnchor.VIEWPORT); textIgnorePlacement(true); textAllowOverlap(true) })
                    style.addLayer(symbolLayer(redUnclusteredLayerId, redSourceId) { filter(Expression.all(Expression.has("pinId"), Expression.neq(Expression.get("cluster"), Expression.literal(true)))); iconImage("pin-red"); iconAllowOverlap(true); iconAnchor(IconAnchor.BOTTOM); iconSize(0.1) })

                    // GREEN source + layers
                    val greenSourceId = "green-pins-source"
                    val greenUnclusteredLayerId = "green-unclustered-layer"
                    val greenClusterIconLayerId = "green-clusters-icon-layer"
                    val greenClusterTextLayerId = "green-clusters-text-layer"

                    style.addSource(
                        geoJsonSource(greenSourceId) {
                            featureCollection(greenFC)
                            cluster(true)
                            clusterRadius(50)
                            clusterMaxZoom(50)
                            clusterMinPoints(2)
                        }
                    )
                    style.addLayer(symbolLayer(greenClusterIconLayerId, greenSourceId) { filter(Expression.has("point_count")); iconImage("pin-green-cluster"); iconAnchor(IconAnchor.BOTTOM); iconAllowOverlap(true); iconSize(0.08) })
                    style.addLayer(symbolLayer(greenClusterTextLayerId, greenSourceId) { filter(Expression.has("point_count")); textField(Expression.get("point_count_abbreviated")); textSize(12.0); textColor("#2F9E44"); textHaloColor("#FFFFFF"); textHaloWidth(4.0); textAnchor(TextAnchor.CENTER); textJustify(TextJustify.CENTER); textTranslate(listOf(8.0, -36.8)); textTranslateAnchor(TextTranslateAnchor.VIEWPORT); textIgnorePlacement(true); textAllowOverlap(true) })
                    style.addLayer(symbolLayer(greenUnclusteredLayerId, greenSourceId) { filter(Expression.all(Expression.has("pinId"), Expression.neq(Expression.get("cluster"), Expression.literal(true)))); iconImage("pin-green"); iconAllowOverlap(true); iconAnchor(IconAnchor.BOTTOM); iconSize(0.1) })
                }

                // --- B4: Set lại Click Handler (vì nó bị xóa khi style thay đổi) ---
                mapView.gestures.addOnMapClickListener { point ->
                    val screenPoint = mapboxMap.pixelForCoordinate(point)
                    val clusterLayerIds = listOf("red-clusters-icon-layer", "green-clusters-icon-layer")
                    val pinLayerIds = listOf("red-unclustered-layer", "green-unclustered-layer")

                    // 1) Ưu tiên click cluster trước
                    mapboxMap.queryRenderedFeatures(
                        RenderedQueryGeometry(screenPoint),
                        RenderedQueryOptions(clusterLayerIds, null)
                    ) { expected ->
                        val clusterHits = expected.value.orEmpty()

                        if (clusterHits.isNotEmpty()) {
                            val feature = clusterHits.first().queriedFeature.feature
                            val props = feature.properties()

                            if (props?.has("cluster") == true && props.get("cluster").asBoolean) {
                                val currentZoom = mapboxMap.cameraState.zoom
                                val pointCount = props.get("point_count")?.asInt ?: 0

                                val zoomIncrement = when {
                                    pointCount > 100 -> 1.5
                                    pointCount > 50 -> 1.5
                                    else -> 1.5
                                }

                                val targetZoom = (currentZoom + zoomIncrement).coerceAtMost(20.0)

                                mapboxMap.flyTo(
                                    CameraOptions.Builder()
                                        .center(feature.geometry() as? Point)
                                        .zoom(targetZoom)
                                        .build(),
                                    MapAnimationOptions.mapAnimationOptions {
                                        duration(1200L)
                                    }
                                )
                                return@queryRenderedFeatures
                            }
                        }

                        // 2) Nếu không trúng cluster thì check pin đơn lẻ
                        mapboxMap.queryRenderedFeatures(
                            RenderedQueryGeometry(screenPoint),
                            RenderedQueryOptions(pinLayerIds, null)
                        ) { expected2 ->
                            val pinHits = expected2.value.orEmpty()
                            if (pinHits.isEmpty()) return@queryRenderedFeatures

                            val f = pinHits.first().queriedFeature.feature
                            if (!f.hasProperty("pinId")) return@queryRenderedFeatures

                            val pinId = f.getStringProperty("pinId")

                            val type = f.getStringProperty("type")

                            // TODO: chuyển màn hình khi click pin đơn lẻ
                            onPinPress(pinId.toInt())
                            Log.d("MapDebug", "Clicked pinId=$pinId type=$type")
                        }
                    }
                    true
                }
            }
        }

        // SEARCH UI
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
                onClear = { viewModel.onClearQuery() },
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

        // CONTROLS
        MapControls(
            onClickStyle = { viewModel.onShowBottomSheet() },
            onClickMyLocation = {
                // Camera follow puck (hiện tại)
                followUser(mapViewportState)
            },
            onClickDiscovery = {
                showDiscoveryGame = true
            }

        )

        // BOTTOM SHEET
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

        if (showDiscoveryGame) {
            PinDiscoveryScreen(
                onDismiss = {
                    showDiscoveryGame = false
                },
                onPinFound = { pinId ->
                    showDiscoveryGame = false

                    // TODO: Navigate to gallery screen với pinId
                },

                )
        }
    }
}

@Composable
private fun MapSearchBar(
    query: String,
    isSearching: Boolean,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(28.dp))
                .background(Color.White, RoundedCornerShape(28.dp)),
            placeholder = { Text("Tìm kiếm địa điểm...", color = Color.Gray) },
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
                    Text("Đang tìm kiếm...", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
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
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
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
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
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
    onClickMyLocation: () -> Unit,
    onClickDiscovery: () -> Unit,
) {
    Column(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Pin Discovery FAB
        FloatingActionButton(
            onClick = onClickDiscovery,
            containerColor = Color(0xFFFF9800),
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Explore,
                contentDescription = "Khám phá ghim",
                tint = Color.White
            )
        }

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

@Composable
private fun RequestLocationPermission(
    onGranted: () -> Unit,
    onDenied: () -> Unit,
) {
    val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                result[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) onGranted() else onDenied()
    }

    // Xin quyền đúng 1 lần khi vào screen
    LaunchedEffect(Unit) {
        launcher.launch(permissions)
    }
}

private fun followUser(mapViewportState: MapViewportState) {
    val followOptions = FollowPuckViewportStateOptions.Builder()
        .pitch(0.0)
        .build()

    mapViewportState.transitionToFollowPuckState(followOptions)
}
