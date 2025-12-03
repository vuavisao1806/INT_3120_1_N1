package com.example.locationpins.data.repository

import com.example.locationpins.data.remote.RetrofitClient
import com.example.locationpins.data.remote.dto.tag.GetPostTagsRequest
import com.example.locationpins.data.remote.dto.tag.TagDto

class TagRepository {

    private val api = RetrofitClient.api

    suspend fun getTagsByPostId(postId: Int): List<TagDto> {
        return api.getPostTags(
            GetPostTagsRequest(postId = postId)
        )
    }
}
