package com.example.locationpins.data.remote.dto.pins

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetPinListByUserIdRequest(
    @SerialName("user_id")
    val userId: Int
)

@Serializable
data class GetPinListInRadiusRequest(
    @SerialName("center_lat")
    val centerLat: Double,
    @SerialName("center_lng")
    val centerLng: Double,
    @SerialName("radius_meters")
    val radiusMeters: Double
)

@Serializable
data class PinDto(
    @SerialName("pin_id")
    val pinId: Int,
    val latitude: Double,
    val longitude: Double,
    @SerialName("created_at")
    val createdAt: String
)

@Serializable
data class PinByCoordRequest(
    @SerialName("center_lat")
    val centerLatitude: Double,
    @SerialName("center_lng")
    val centerLongitude: Double,
    @SerialName("radius_meters")
    val radiusMeters: Double
)

@Serializable
data class PinByCoordResponse(
    @SerialName("pin_id")
    val pinId: Int,
    @SerialName("is_new_pin")
    val isNewPin: Boolean
)

