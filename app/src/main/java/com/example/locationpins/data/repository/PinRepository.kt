package com.example.locationpins.data.repository



import com.example.locationpins.data.remote.RetrofitClient
import com.example.locationpins.data.remote.dto.pins.FindRandomPinRequest
import com.example.locationpins.data.remote.dto.pins.GetPinListByUserIdRequest
import com.example.locationpins.data.remote.dto.pins.GetPinListInRadiusRequest
import com.example.locationpins.data.remote.dto.pins.PinByCoordRequest
import com.example.locationpins.data.remote.dto.pins.PinByCoordResponse
import com.example.locationpins.data.remote.dto.pins.PinDto
import com.example.locationpins.data.remote.dto.pins.RandomPinResponse


class PinRepository {

    private val api = RetrofitClient.api

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

    suspend fun getPinIdByCoordinates(
        centerLatitude: Double,
        centerLongitude: Double,
        radiusMeters: Double = 50.0
    ): PinByCoordResponse {
        return api.getPinIdByCoordinates(
            request = PinByCoordRequest(
                centerLatitude = centerLatitude,
                centerLongitude = centerLongitude,
                radiusMeters = radiusMeters
            )
        )
    }

    suspend fun findRandomPin(
        userLat: Double,
        userLng: Double,
        targetDistance: Int
    ): RandomPinResponse {
        return api.findRandomPin(
            FindRandomPinRequest(
                userLat = userLat,
                userLng = userLng,
                targetDistance = targetDistance
            )
        )
    }
}
