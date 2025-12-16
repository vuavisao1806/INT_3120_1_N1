package com.example.locationpins.data.remote.dto.tag

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TagDto(
    @SerialName("tag_id")
    val tagId: Int,

    @SerialName("name")
    val name: String
)

@Serializable
data class GetPostTagsRequest(
    @SerialName("post_id")
    val postId: Int
)


@Serializable
data class GoogleLabelResponse(
    @SerialName("tags")
    val tags: List<String>
)

@Serializable
data class AssignTagsRequest(
    @SerialName("post_id")
    val postId: Int,
    @SerialName("user_id")
    val userId: Int,
    @SerialName("tags")
    val tags: List<String>
)

@Serializable
data class UserFavoriteTagsRequest(
    @SerialName("user_id")
    val userId: Int,
    @SerialName("number_tags")
    val numberTags: Int
)