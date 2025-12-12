package com.example.locationpins.data.remote.dto.post

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InsertPostRequest(
    @SerialName("pin_id")
    val pinId: Int,
    @SerialName("user_id")
    val userId: Int,
    val title: String,
    val body: String,
    @SerialName("image_url")
    val imageUrl: String,
    val status: String = "DEFAULT"
)

@Serializable
data class InsertPostSuccess(
    @SerialName("insert_post_success")
    val insertPostSuccess: Boolean
)
