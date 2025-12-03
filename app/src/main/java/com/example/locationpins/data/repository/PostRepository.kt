package com.example.locationpins.data.repository

import com.example.locationpins.data.remote.RetrofitClient
import com.example.locationpins.data.remote.dto.comment.CommentDto
import com.example.locationpins.data.remote.dto.comment.GetPostCommentsRequest
import com.example.locationpins.data.remote.dto.post.GetNewsfeedRequest
import com.example.locationpins.data.remote.dto.post.GetPostRequest
import com.example.locationpins.data.remote.dto.post.PostDto


class PostRepository {

    private val api = RetrofitClient.api

    suspend fun getPost(postId: Int): PostDto {
        return api.getPost(
            GetPostRequest(postId = postId)
        )
    }

    suspend fun getNewsfeed(
        userId: Int,
        limit: Int = 20,
        offset: Int = 0
    ): List<PostDto> {
        return api.getNewsfeed(
            GetNewsfeedRequest(
                userId = userId,
                limit = limit,
                offset = offset
            )
        )
    }


}