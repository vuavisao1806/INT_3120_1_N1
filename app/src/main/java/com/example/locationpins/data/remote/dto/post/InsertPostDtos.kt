package com.example.locationpins.data.remote.dto.post

import kotlinx.serialization.Serializable

@Serializable
data class InsertPostRequest(
    val pin_id: Int,
    val user_id: Int,
    val title: String,
    val body: String,
    val image_url: String,
    val status: String
)

@Serializable
data class InsertPostSuccess(
    val insert_post_success: Boolean
)
