package com.example.locationpins.data.remote.dto.comment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateCommentRequest(
    @SerialName("post_id")
    val postId: Int,

    @SerialName("user_id")
    val userId: Int,

    @SerialName("content")
    val content: String
)

@Serializable
data class CancelCommentRequest(
    @SerialName("comment_id")
    val commentId: Int,

    @SerialName("user_id")
    val userId: Int
)

@Serializable
data class GetPostCommentsRequest(
    @SerialName("post_id")
    val postId: Int
)

@Serializable
data class CommentDto(
    @SerialName("comment_id")
    val commentId: Int,

    @SerialName("post_id")
    val postId: Int,

    @SerialName("user_id")
    val userId: Int,

    @SerialName("user_name")
    val userName: String,

    @SerialName("avatar_url")
    val avatarUrl: String,

    @SerialName("content")
    val content: String,

    @SerialName("created_at")
    val createdAt: String
)
