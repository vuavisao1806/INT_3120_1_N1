package com.example.locationpins.ui.screen.map

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.SearchOptions
import com.mapbox.search.SearchSuggestionsCallback
import com.mapbox.search.result.SearchSuggestion
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.locationpins.data.repository.PinRepository
import com.mapbox.geojson.Point
import com.mapbox.search.ApiType
import com.mapbox.search.SearchSelectionCallback
import com.mapbox.search.result.SearchResult

class MapViewModel(
    private val pinRepo: PinRepository = PinRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState

    private val searchEngine: SearchEngine by lazy {
        val settings = SearchEngineSettings()
        SearchEngine.createSearchEngineWithBuiltInDataProviders(
            apiType = ApiType.GEOCODING,
            settings = settings
        )
    }
    private var searchJob: Job? = null


    init {
        loadPins(1) // đoạn này là mock userId=1, sau app phải lưu userId hiện tại lại
    }

    fun onShowBottomSheet() {
        _uiState.value = _uiState.value.copy(showBottomSheet = true)
    }

    fun onHideBottomSheet() {
        _uiState.value = _uiState.value.copy(showBottomSheet = false)
    }

    fun onMapStyleSelected(styleUri: String) {
        _uiState.value = _uiState.value.copy(currentStyleUri = styleUri)
    }

    /**
     * Thực hiện một lần search với Mapbox SearchEngine.
     *
     * @param query        Chuỗi người dùng nhập (đã trim).
     * @param searchEngine Đối tượng Mapbox dùng để gọi API search.
     * @param onResult     Hàm callback được gọi KHI SEARCH THÀNH CÔNG,
     *                     nhận vào danh sách gợi ý (List<SearchSuggestion>).
     * @param onError      Hàm callback được gọi KHI CÓ LỖI trong quá trình search.
     */
    fun runSearch(
        query: String,
        onResult: (List<SearchSuggestion>) -> Unit,
        onError: (Exception) -> Unit,
    ) {
        // Cấu hình search: giới hạn tối đa 10 gợi ý
        val options = SearchOptions(limit = 10)

        // Gọi hàm search của Mapbox
        searchEngine.search(
            query,
            options,
            // Tạo một instance (đối tượng) ẩn danh implement interface SearchSuggestionsCallback.
            // Mapbox sẽ GIỮ đối tượng này lại và TỰ GỌI 2 hàm onError / onSuggestions
            // khi có kết quả từ server.
            object : SearchSuggestionsCallback {
                /**
                 * Hàm này được Mapbox gọi khi có lỗi xảy ra trong quá trình search.
                 */
                override fun onError(e: Exception) {
                    // Chuyển tiếp lỗi ra ngoài cho caller thông qua callback onError
                    // (onError được truyền vào runSearch từ bên ngoài).
                    onError(e)
                }

                /**
                 * Hàm này được Mapbox gọi khi search thành công và có danh sách gợi ý.
                 *
                 * @param suggestions Danh sách các SearchSuggestion trả về.
                 * @param responseInfo Thông tin thêm về response (ở đây không dùng tới).
                 */
                override fun onSuggestions(
                    suggestions: List<SearchSuggestion>,
                    responseInfo: com.mapbox.search.ResponseInfo
                ) {
                    // Chuyển danh sách gợi ý ra ngoài cho caller xử lý
                    // (caller sẽ update UI trong onResult).
                    onResult(suggestions)
                }
            }
        )
    }

    /**
     * Hàm này được gọi mỗi khi query (text user nhập) thay đổi.
     * Thường sẽ được gọi từ trong UI.
     *
     * Nhiệm vụ:
     *  - Cập nhật query vào uiState.
     *  - Áp dụng debounce (chờ user gõ xong ~500ms mới search).
     *  - Không search nếu query quá ngắn (< 2 ký tự).
     *  - Khi search xong thì cập nhật suggestions + trạng thái isSearching.
     */
    fun onQueryChange(
        newQuery: String,
    ) {
        // Cập nhật query mới vào state để UI hiển thị
        _uiState.value = _uiState.value.copy(query = newQuery)

        // Huỷ coroutine search cũ (nếu có) để thực hiện DEBOUNCE:
        // mỗi lần user gõ thêm ký tự thì cancel lần search trước,
        // chỉ giữ lại lần search ứng với lần gõ cuối cùng.
        searchJob?.cancel()

        // Bỏ khoảng trắng thừa ở đầu/cuối
        val trimmedQuery = newQuery.trim()

        // Nếu query quá ngắn (< 2 ký tự) thì không gọi API,
        // đồng thời xoá danh sách gợi ý và tắt trạng thái loading.
        if (trimmedQuery.length < 2) {
            _uiState.value = _uiState.value.copy(
                suggestions = emptyList(),
                isSearching = false
            )
            return
        }

        // Tạo một coroutine mới để thực hiện search sau khi debounce
        searchJob = viewModelScope.launch {
            // Đợi 500ms kể từ lần gõ cuối cùng trước khi thật sự gọi search
            // → đây chính là cơ chế DEBOUNCE.
            delay(500)

            // Báo cho UI biết là đang search (có thể show loading, spinner, v.v.)
            _uiState.value = _uiState.value.copy(isSearching = true)

            // Gọi hàm runSearch đã viết ở trên.
            // Ở đây ta truyền vào 2 callback:
            //  - onResult: xử lý khi Mapbox trả về gợi ý.
            //  - onError: xử lý khi search bị lỗi.
            runSearch(
                query = trimmedQuery,
                onResult = { list ->
                    // KHI SEARCH THÀNH CÔNG:
                    //  - cập nhật danh sách gợi ý vào uiState
                    //  - tắt trạng thái loading
                    _uiState.value = _uiState.value.copy(
                        suggestions = list,
                        isSearching = false
                    )
                },
                onError = {
                    // KHI CÓ LỖI:
                    //  - xoá danh sách gợi ý (hoặc có thể giữ lại tuỳ design)
                    //  - tắt trạng thái loading
                    //  - có thể log/hiển thị message lỗi (chưa thêm)
                    _uiState.value = _uiState.value.copy(
                        suggestions = emptyList(),
                        isSearching = false
                    )
                }
            )
        }
    }

    fun onClearQuery() {
        _uiState.value = _uiState.value.copy(
            query = "",
            suggestions = emptyList(),
            isSearching = false
        )
    }

    /**
     * User bấm chọn một suggestion trong list.
     * Ở đây vừa:
     *  - cập nhật query
     *  - clear suggestions
     *  - gọi searchEngine.select để lấy toạ độ
     *  - set cameraCoordinate để UI move camera
     */
    fun onSuggestionSelected(suggestion: SearchSuggestion) {
        // Clear list & set query trước cho UI phản hồi nhanh
        _uiState.value = _uiState.value.copy(
            query = suggestion.name,
            suggestions = emptyList()
        )

        searchEngine.select(
            suggestion,
            object : SearchSelectionCallback {
                override fun onError(e: Exception) {
                    // Có thể log nếu cần, tạm thời bỏ qua
                }

                override fun onSuggestions(
                    suggestions: List<SearchSuggestion>,
                    responseInfo: com.mapbox.search.ResponseInfo
                ) {
                    // category suggestions - ignore
                }

                override fun onResult(
                    suggestion: SearchSuggestion,
                    result: SearchResult,
                    responseInfo: com.mapbox.search.ResponseInfo
                ) {
                    val coordinate: Point = result.coordinate ?: return
                    _uiState.value = _uiState.value.copy(
                        cameraCoordinate = coordinate
                    )
                }

                override fun onResults(
                    suggestion: SearchSuggestion,
                    results: List<SearchResult>,
                    responseInfo: com.mapbox.search.ResponseInfo
                ) {
                    val first = results.firstOrNull() ?: return
                    val coordinate: Point = first.coordinate ?: return
                    _uiState.value = _uiState.value.copy(
                        cameraCoordinate = coordinate
                    )
                }
            }
        )
    }

    /**
     * Gọi sau khi UI đã move camera xong để reset state,
     * tránh việc camera bị move lại nhiều lần.
     */
    fun onCameraMoved() {
        _uiState.value = _uiState.value.copy(
            cameraCoordinate = null
        )
    }

    fun loadPins(userId: Int) {
        viewModelScope.launch {
            try {
                val pins = pinRepo.getPinsByUserId(userId)
                Log.d("MapViewModel", "Loaded ${pins.size} pins for userId=$userId")
                _uiState.value = _uiState.value.copy(
                    pinList = pins
                )
            } catch (e: Exception) {
                Log.e("MapViewModel", "Error loading pins for userId=$userId", e)
            }
        }
    }

    fun onUserLocationChanged(point: Point) {
        _uiState.value = _uiState.value.copy(
            userLocation = point
        )
    }

}

