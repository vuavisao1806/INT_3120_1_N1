package com.example.locationpins.data.repository

import android.util.Log
import com.example.locationpins.data.remote.RetrofitClient
import com.example.locationpins.data.remote.dto.post.GetNewsfeedRequest
import com.example.locationpins.data.remote.dto.post.GetPinPreviewRequest
import com.example.locationpins.data.remote.dto.post.GetPostByPinIdRequestFromMapScreenRequest
import com.example.locationpins.data.remote.dto.post.GetPostByPinRequest
import com.example.locationpins.data.remote.dto.post.GetPostRequest
import com.example.locationpins.data.remote.dto.post.InsertPostRequest
import com.example.locationpins.data.remote.dto.post.InsertPostSuccess
import com.example.locationpins.data.remote.dto.post.PinPreview
import com.example.locationpins.data.remote.dto.post.PostByPinResponse
import com.example.locationpins.data.remote.dto.post.PostDto
import com.example.locationpins.data.remote.dto.tag.GoogleLabelResponse
import com.example.locationpins.data.remote.dto.user.GetPostByUserRequest
import okhttp3.MultipartBody


class PostRepository {

    private val api = RetrofitClient.api

    suspend fun insertPost(
        pinId: Int,
        userId: Int,
        title: String,
        content: String,
        imageUrl: String,
        status: String
    ): InsertPostSuccess {
        val insertPostRequest = InsertPostRequest(
            pinId = pinId,
            userId = userId,
            title = title,
            body = content,
            imageUrl = imageUrl,
            status = status
        )
        return api.insertPost(insertPostRequest)
    }

    suspend fun getPost(postId: Int): PostDto {
        return api.getPost(
            GetPostRequest(postId = postId)
        )
    }

    suspend fun getNewsfeed(
        userId: Int,
        limit: Int = 20,
        offset: Int = 0,
        tagName: String? = null
    ): List<PostDto> {
        return api.getNewsfeed(
            GetNewsfeedRequest(
                userId = userId,
                limit = limit,
                offset = offset,
                tagName = tagName,
            )
        )
    }

    suspend fun getPostByPinIdRequestFromMapScreen(
        pinId: Int,
        limit: Int = 20,
        offset: Int = 0
    ): List<PostDto> {
        Log.d("MapDebuggg123", "Clicked pinId=$pinId")
        return api.getPostByPinIdRequestFromMapScreen(
            GetPostByPinIdRequestFromMapScreenRequest(
                pinId = pinId,
                limit = limit,
                offset = offset
            )
        )
    }

    suspend fun getPreviewPins(userId: Int): List<PinPreview> {
        return api.getPinPreview(GetPinPreviewRequest(userId))
    }

    suspend fun getPostByPin(pinId: Int): List<PostByPinResponse> {
        return api.getPostByPin(GetPostByPinRequest(pinId))
    }

    suspend fun getPostByUser(userId: Int): List<PostByPinResponse> {
        return api.getPostByUser(GetPostByUserRequest(userId))
    }
}