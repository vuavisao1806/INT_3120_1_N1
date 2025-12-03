package com.example.locationpins.data.remote.dto.post


import com.mapbox.search.common.SearchAddressCountry
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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