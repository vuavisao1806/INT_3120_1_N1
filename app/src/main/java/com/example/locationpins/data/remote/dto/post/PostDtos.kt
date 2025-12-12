package com.example.locationpins.data.remote.dto.post


import com.mapbox.search.common.SearchAddressCountry
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Multipart
import retrofit2.http.POST

@Serializable
data class GetPostRequest(
    @SerialName("post_id")
    val postId: Int
)

@Serializable
data class PostDto(
    @SerialName("post_id")
    val postId: Int,
    @SerialName("pin_id")
    val pinId: Int,
    @SerialName("title")
    val title: String,
    @SerialName("body")
    val body: String,
    @SerialName("image_url")
    val imageUrl: String,
    @SerialName("user_id")
    val userId: Int,
    @SerialName("user_name")
    val userName: String,
    @SerialName("avatar_url")
    val avatarUrl: String,
    @SerialName("status")
    val status: String,
    @SerialName("reaction_count")
    val reactionCount: Int,
    @SerialName("comment_count")
    val commentCount: Int,
    @SerialName("created_at")
    val createdAt: String
)

@Serializable
data class GetNewsfeedRequest(
    @SerialName("user_id")
    val userId: Int,

    @SerialName("limit")
    val limit: Int = 20,

    @SerialName("offset")
    val offset: Int = 0
)

@Serializable
data class GetPinPreviewRequest(
    @SerialName("user_id")
    val userId: Int
)

@Serializable
data class PinPreview(
    @SerialName("pin_id")
    val pinId: Int,
    @SerialName("image_url")
    val pinImage: String,
    @SerialName("cnt")
    val quantity: Long
)

@Serializable
data class GetPostByPinRequest(
    @SerialName("pin_id")
    val pinId: Int
)

@Serializable
data class PostByPinResponse(
    @SerialName("post_id")
    val postId: Int,
    @SerialName("pin_id")
    val pinId: Int,
    @SerialName("title")
    val title: String,
    @SerialName("body")
    val body: String,
    @SerialName("image_url")
    val imageUrl: String,
    @SerialName("user_id")
    val userId: Int,
    @SerialName("status")
    val status: String,
    @SerialName("reaction_count")
    val reactionCount: Int,
    @SerialName("comment_count")
    val commentCount: Int,
    @SerialName("created_at")
    val createdAt: String
)

@Serializable
data class SensitiveTextRespond(
    @SerialName("is_sensitive")
    val isSensitive: Boolean
)