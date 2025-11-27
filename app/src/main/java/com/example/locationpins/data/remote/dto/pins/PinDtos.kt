package com.example.locationpins.data.remote.dto.pins

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetPinListByUserIdRequest(
    @SerialName("user_id")
    val userId: Int
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
