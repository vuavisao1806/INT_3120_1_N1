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
