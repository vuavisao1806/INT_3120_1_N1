package com.example.locationpins.ui.screen.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationpins.data.remote.dto.pins.PinDto
import com.example.locationpins.data.repository.PinRepository
import com.mapbox.geojson.Point
import com.mapbox.search.ApiType
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.SearchOptions
import com.mapbox.search.SearchSelectionCallback
import com.mapbox.search.SearchSuggestionsCallback
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MapViewModel(
    private val pinRepo: PinRepository = PinRepository()
) : ViewModel() {

    companion object {
        private const val TAG = "MapViewModel"
        private const val RADIUS_METERS = 100000.0
    }

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val searchEngine: SearchEngine by lazy {
        val settings = SearchEngineSettings()
        SearchEngine.createSearchEngineWithBuiltInDataProviders(
            apiType = ApiType.GEOCODING,
            settings = settings
        )
    }

    private var searchJob: Job? = null
    private var pollingJob: Job? = null

    init {
        // ✅ Lắng nghe vị trí từ LocationManager (nguồn duy nhất cho logic app)
        observeUserLocation()

        // ✅ Load pin định kỳ
        startPeriodicPinLoading(userId = 1)
    }

    private fun observeUserLocation() {
        viewModelScope.launch {
            LocationManager.location.collect { loc ->
                loc ?: return@collect
                val point = Point.fromLngLat(loc.longitude, loc.latitude)

                _uiState.update { state ->
                    state.copy(userLocation = point)
                }
            }
        }
    }

    fun onMyLocationClicked(): Point? {
        val loc = LocationManager.location.value
        return loc?.let { Point.fromLngLat(it.longitude, it.latitude) }
    }

    fun onShowBottomSheet() {
        _uiState.update { it.copy(showBottomSheet = true) }
    }

    fun onHideBottomSheet() {
        _uiState.update { it.copy(showBottomSheet = false) }
    }

    fun onMapStyleSelected(styleUri: String) {
        _uiState.update { it.copy(currentStyleUri = styleUri) }
    }

    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
        searchJob?.cancel()

        val trimmedQuery = newQuery.trim()
        if (trimmedQuery.length < 2) {
            _uiState.update { it.copy(suggestions = emptyList(), isSearching = false) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(500)
            _uiState.update { it.copy(isSearching = true) }

            runMapboxSearch(
                query = trimmedQuery,
                onResult = { suggestions ->
                    _uiState.update { it.copy(suggestions = suggestions, isSearching = false) }
                },
                onError = { e ->
                    Log.e(TAG, "Search failed", e)
                    _uiState.update { it.copy(suggestions = emptyList(), isSearching = false) }
                }
            )
        }
    }

    private fun runMapboxSearch(
        query: String,
        onResult: (List<SearchSuggestion>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        searchEngine.search(
            query,
            SearchOptions(limit = 10),
            object : SearchSuggestionsCallback {
                override fun onSuggestions(
                    suggestions: List<SearchSuggestion>,
                    responseInfo: com.mapbox.search.ResponseInfo
                ) = onResult(suggestions)

                override fun onError(e: Exception) = onError(e)
            }
        )
    }

    fun onSuggestionSelected(suggestion: SearchSuggestion) {
        _uiState.update { it.copy(query = suggestion.name, suggestions = emptyList()) }

        searchEngine.select(suggestion, object : SearchSelectionCallback {
            override fun onError(e: Exception) {
                Log.e(TAG, "Select location failed", e)
            }

            override fun onSuggestions(
                suggestions: List<SearchSuggestion>,
                responseInfo: com.mapbox.search.ResponseInfo
            ) {
                // ignore
            }

            override fun onResult(
                suggestion: SearchSuggestion,
                result: SearchResult,
                responseInfo: com.mapbox.search.ResponseInfo
            ) {
                result.coordinate?.let { point ->
                    _uiState.update { it.copy(cameraCoordinate = point) }
                }
            }

            override fun onResults(
                suggestion: SearchSuggestion,
                results: List<SearchResult>,
                responseInfo: com.mapbox.search.ResponseInfo
            ) {
                results.firstOrNull()?.coordinate?.let { point ->
                    _uiState.update { it.copy(cameraCoordinate = point) }
                }
            }
        })
    }

    fun onClearQuery() {
        _uiState.update { it.copy(query = "", suggestions = emptyList(), isSearching = false) }
    }

    fun onCameraMoved() {
        _uiState.update { it.copy(cameraCoordinate = null) }
    }

    private fun startPeriodicPinLoading(userId: Int) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                loadPins(userId)
                delay(5000)
            }
        }
    }

    private fun loadPins(userId: Int) {
        viewModelScope.launch {
            try {
                val currentLocation = _uiState.value.userLocation

                val ownedPins = try {
                    pinRepo.getPinsByUserId(userId)
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading owned pins", e)
                    emptyList()
                }

                val radiusPins: List<PinDto> = if (currentLocation != null) {
                    try {
                        pinRepo.getPinsInRadius(
                            centerLat = currentLocation.latitude(),
                            centerLng = currentLocation.longitude(),
                            radiusMeters = RADIUS_METERS
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error loading radius pins", e)
                        emptyList()
                    }
                } else {
                    emptyList()
                }

                val ownedIds = ownedPins.map { it.pinId }.toSet()
                val greenOnly = radiusPins.filter { it.pinId !in ownedIds }

                _uiState.update {
                    it.copy(
                        redPinList = ownedPins,
                        greenPinList = greenOnly
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, "Critical error in loadPins", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
        searchJob?.cancel()
    }
}
