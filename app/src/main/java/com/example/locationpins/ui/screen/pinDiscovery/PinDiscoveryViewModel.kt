package com.example.locationpins.ui.screen.pinDiscovery

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationpins.data.repository.PinRepository
import com.example.locationpins.ui.screen.map.LocationManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.*
import kotlin.random.Random
import android.content.Context
import com.example.locationpins.ui.component.CompassSensor

data class PinDiscoveryUiState(
    val gameState: GameState = GameState.Initial,
    val selectedDistance: Int = 100, // 50, 100, 200, 500
    val targetPinId: Int? = null,
    val targetLatitude: Double? = null,
    val targetLongitude: Double? = null,
    val currentDistance: Float? = null,
    val lastHint: String? = null,
    val hintType: HintType? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val compassRotation: Float = 0f
)

sealed class GameState {
    object Initial : GameState()
    object Searching : GameState()
    object Found : GameState()
}

enum class HintType {
    DISTANCE, DIRECTION,
//    CREATIVE
}

class PinDiscoveryViewModel(
    private val pinRepository: PinRepository = PinRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PinDiscoveryUiState())
    val uiState: StateFlow<PinDiscoveryUiState> = _uiState.asStateFlow()

    private var hintJob: Job? = null
    private var distanceCheckJob: Job? = null
    private var compassUpdateJob: Job? = null

    private var compassSensor: CompassSensor? = null

    companion object {
        private const val HINT_INTERVAL_MS = 5000L // 5 seconds
        private const val DISTANCE_CHECK_INTERVAL_MS = 3000L // 3 seconds
        private const val SUCCESS_THRESHOLD_METERS = 10f
    }

    fun selectDistance(distance: Int) {
        _uiState.update { it.copy(selectedDistance = distance) }
    }

    fun startGame() {
        val userLocation = LocationManager.location.value
        if (userLocation == null) {
            _uiState.update {
                it.copy(error = "Không thể lấy vị trí hiện tại")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Chỉ gọi API 1 lần để lấy pin target
                val response = pinRepository.findRandomPin(
                    userLat = userLocation.latitude,
                    userLng = userLocation.longitude,
                    targetDistance = _uiState.value.selectedDistance
                )

                // Lưu thông tin pin vào state
                _uiState.update {
                    it.copy(
                        gameState = GameState.Searching,
                        targetPinId = response.pinId,
                        targetLatitude = response.latitude,
                        targetLongitude = response.longitude,
                        isLoading = false,
                        lastHint = "Đã tìm được ghim mục tiêu! Hãy bắt đầu tìm kiếm nhé 🎯"
                    )
                }

                // Bắt đầu timer hint và check distance (tất cả ở local)
                startHintTimer()
                startDistanceCheck()
                startCompassUpdates()

            } catch (e: Exception) {
                Log.e("PinDiscovery", "Error starting game: ${e.message}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Không thể tìm ghim: ${e.message}"
                    )
                }
            }
        }
    }

    private fun startHintTimer() {
        hintJob?.cancel()
        hintJob = viewModelScope.launch {
            while (true) {
                delay(HINT_INTERVAL_MS)
                generateLocalHint()
            }
        }
    }

    private fun generateLocalHint() {
        val state = _uiState.value
        val targetLat = state.targetLatitude ?: return
        val targetLng = state.targetLongitude ?: return
        val userLocation = LocationManager.location.value ?: return

        // Tính khoảng cách hiện tại
        val distance = calculateDistance(
            userLocation.latitude,
            userLocation.longitude,
            targetLat,
            targetLng
        )

        // Chọn ngẫu nhiên loại hint
        val hintType = HintType.values().random()

        val hintText = when (hintType) {
            HintType.DISTANCE -> {
                when {
                    distance < 50 -> "Bạn đang rất gần! Chỉ còn ${distance.toInt()} mét nữa thôi!"
                    distance < 100 -> "Gần rồi! Còn khoảng ${distance.toInt()} mét."
                    else -> "Bạn còn cách mục tiêu ${distance.toInt()} mét."
                }
            }
            HintType.DIRECTION -> {
                val direction = calculateDirection(
                    userLocation.latitude,
                    userLocation.longitude,
                    targetLat,
                    targetLng
                )
                "Mục tiêu nằm ở hướng $direction."
            }
//            HintType.CREATIVE -> {
//                listOf(
//                    "Hãy tin vào trực giác của bạn!",
//                    "Phía trước có gì đang chờ bạn khám phá...",
//                    "Bạn đang đi đúng hướng rồi!",
//                    "Hãy quan sát xung quanh kỹ hơn...",
//                    "Điều bất ngờ đang ở gần đây!",
//                    "Cảm nhận được năng lượng của ghim chưa? 🧭",
//                    "Mục tiêu đang gần hơn bạn nghĩ đấy!",
//                    "Có vẻ như bạn đang tiến gần..."
//                ).random()
//            }
        }

        _uiState.update {
            it.copy(
                lastHint = hintText,
                hintType = hintType,
                currentDistance = distance
            )
        }
    }

    private fun startDistanceCheck() {
        distanceCheckJob?.cancel()
        distanceCheckJob = viewModelScope.launch {
            while (true) {
                delay(DISTANCE_CHECK_INTERVAL_MS)
                checkDistance()
            }
        }
    }

    private fun checkDistance() {
        val state = _uiState.value
        if (state.gameState != GameState.Searching) return

        val targetLat = state.targetLatitude ?: return
        val targetLng = state.targetLongitude ?: return
        val userLocation = LocationManager.location.value ?: return

        val distance = calculateDistance(
            userLocation.latitude,
            userLocation.longitude,
            targetLat,
            targetLng
        )

        _uiState.update { it.copy(currentDistance = distance) }

        if (distance <= SUCCESS_THRESHOLD_METERS) {
            onPinFound()
        }
    }

    private fun onPinFound() {
        hintJob?.cancel()
        distanceCheckJob?.cancel()
        compassUpdateJob?.cancel()
        compassSensor?.stop()

        _uiState.update {
            it.copy(
                gameState = GameState.Found,
                lastHint = "🎉 Chúc mừng! Bạn đã tìm thấy ghim!"
            )
        }
    }

    // Tính khoảng cách bằng công thức Haversine
    private fun calculateDistance(
        lat1: Double,
        lng1: Double,
        lat2: Double,
        lng2: Double
    ): Float {
        val earthRadius = 6371000.0 // meters

        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2) * sin(dLng / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return (earthRadius * c).toFloat()
    }

    // Tính hướng (bearing) và chuyển thành tên hướng
    private fun calculateDirection(
        lat1: Double,
        lng1: Double,
        lat2: Double,
        lng2: Double
    ): String {
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val dLng = Math.toRadians(lng2 - lng1)

        val x = sin(dLng) * cos(lat2Rad)
        val y = cos(lat1Rad) * sin(lat2Rad) -
                sin(lat1Rad) * cos(lat2Rad) * cos(dLng)

        val bearing = atan2(x, y)
        val bearingDegrees = (Math.toDegrees(bearing) + 360) % 360

        // Chuyển đổi góc thành 8 hướng
        val directions = listOf(
            "Bắc", "Đông Bắc", "Đông", "Đông Nam",
            "Nam", "Tây Nam", "Tây", "Tây Bắc"
        )
        val index = ((bearingDegrees + 22.5) / 45).toInt() % 8
        return directions[index]
    }

    fun resetGame() {
        hintJob?.cancel()
        distanceCheckJob?.cancel()
        compassUpdateJob?.cancel()
        compassSensor?.stop()
        _uiState.value = PinDiscoveryUiState()
    }

    override fun onCleared() {
        super.onCleared()
        hintJob?.cancel()
        distanceCheckJob?.cancel()
        compassUpdateJob?.cancel()
        compassSensor?.stop()
    }

    fun initCompass(context: Context) {
        if (compassSensor == null) {
            compassSensor = CompassSensor(context)
            startCompassUpdates()
        }
    }

    private fun startCompassUpdates() {
        compassSensor?.start()

        compassUpdateJob?.cancel()
        compassUpdateJob = viewModelScope.launch {
            compassSensor?.azimuth?.collect { deviceAzimuth ->
                // Đơn giản chỉ cập nhật góc quay la bàn
                _uiState.update {
                    it.copy(compassRotation = -deviceAzimuth)  // Âm để kim chỉ Bắc
                }
            }
        }
    }
}