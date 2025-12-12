package com.example.locationpins.data.remote.dto.sensitive

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckIsSensitiveText(
    val text: String,
)

@Serializable
data class IsSensitiveTextRespond(
    @SerialName("is_sensitive")
    val isSensitive: Boolean
)