package com.example.locationpins.data.repository

import com.example.locationpins.data.remote.RetrofitClient
import com.example.locationpins.data.remote.dto.tag.AssignTagsRequest
import com.example.locationpins.data.remote.dto.tag.GetPostTagsRequest
import com.example.locationpins.data.remote.dto.tag.GoogleLabelResponse
import com.example.locationpins.data.remote.dto.tag.TagDto
import com.example.locationpins.data.remote.dto.tag.UserFavoriteTagsRequest
import okhttp3.MultipartBody

class TagRepository {

    private val api = RetrofitClient.api

    suspend fun getTagsByPostId(postId: Int): List<TagDto> {
        return api.getPostTags(
            GetPostTagsRequest(postId = postId)
        )
    }

    // Upload ảnh (MultipartBody.Part có sẵn) -> lấy top K tags
    suspend fun getGoogleLabelsTopK(filePart: MultipartBody.Part, k: Int = 3): GoogleLabelResponse {
        return api.getGoogleLabelsTopK(file = filePart, k = k)
    }

    // Gửi list tag lên backend để insert/update 3 bảng
    suspend fun assignTags(postId: Int, userId: Int, tags: List<String>): Unit {
        return api.assignTags(
            AssignTagsRequest(
                postId = postId,
                userId = userId,
                tags = tags
            )
        )
    }

    suspend fun getFavoriteTagsByUserId(userId: Int, numberTags: Int = 3): Set<TagDto> {
        return api.getFavoriteTagsByUserId(
            body = UserFavoriteTagsRequest(
                userId = userId,
                numberTags = numberTags
            )
        )
    }
}
