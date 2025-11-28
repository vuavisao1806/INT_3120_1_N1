package com.example.locationpins.data.repository



import com.example.locationpins.data.remote.RetrofitClient
import com.example.locationpins.data.remote.dto.pins.GetPinListByUserIdRequest
import com.example.locationpins.data.remote.dto.pins.GetPinListInRadiusRequest
import com.example.locationpins.data.remote.dto.pins.PinDto

class PinRepository {

    private val api = RetrofitClient.api  // <-- dùng ApiService ở đây

    suspend fun getPinsByUserId(userId: Int): List<PinDto> {
        return api.getPinsByUserId(
            GetPinListByUserIdRequest(userId = userId)
        )
    }
    suspend fun getPinsInRadius(
        centerLat: Double,
        centerLng: Double,
        radiusMeters: Double
    ): List<PinDto> {
        return api.getPinsByInRadius(
            GetPinListInRadiusRequest(
                centerLat = centerLat,
                centerLng = centerLng,
                radiusMeters = radiusMeters
            )
        )
    }
}
