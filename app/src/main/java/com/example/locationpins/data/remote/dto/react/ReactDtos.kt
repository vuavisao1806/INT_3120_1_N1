package com.example.locationpins.data.remote.dto.react

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class ReactionRequest(
    @SerialName("post_id")
    val postId: Int,

    @SerialName("user_id")
    val userId: Int
)

@Serializable
data class CancelReactionRequest(
    @SerialName("post_id")
    val postId: Int,

    @SerialName("user_id")
    val userId: Int
)

@Serializable
data class CheckPostReactRequest(
    @SerialName("post_id")
    val postId: Int,
    @SerialName("user_id")
    val userId: Int
)

@Serializable
data class CheckPostReactResponse(
    @SerialName("have_reaction")
    val haveReaction: Boolean
)

@Serializable
data class ReactionDto(
    @SerialName("post_id")
    val postId: Int,
    @SerialName("user_id")
    val userId: Int,
    @SerialName("created_at")
    val createdAt: String
)