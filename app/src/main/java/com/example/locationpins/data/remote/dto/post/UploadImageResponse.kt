package com.example.locationpins.data.remote.dto.post

import kotlinx.serialization.Serializable

@Serializable
data class UploadImageResponse(
    val success: Boolean,
    val url: String,
    val path: String
)
