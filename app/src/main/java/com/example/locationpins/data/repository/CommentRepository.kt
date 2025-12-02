package com.example.locationpins.data.repository


import com.example.locationpins.data.remote.RetrofitClient
import com.example.locationpins.data.remote.dto.comment.CancelCommentRequest
import com.example.locationpins.data.remote.dto.comment.CommentDto
import com.example.locationpins.data.remote.dto.comment.CreateCommentRequest
import com.example.locationpins.data.remote.dto.comment.GetPostCommentsRequest

class CommentRepository {

    private val api = RetrofitClient.api

    suspend fun createComment(
        postId: Int,
        userId: Int,
        content: String
    ) {
        api.createComment(
            CreateCommentRequest(
                postId = postId,
                userId = userId,
                content = content
            )
        )
    }

    suspend fun cancelComment(
        commentId: Int,
        userId: Int
    ) {
        api.cancelComment(
            CancelCommentRequest(
                commentId = commentId,
                userId = userId
            )
        )
    }
    suspend fun getPostComments(postId: Int): List<CommentDto> {
        return api.getPostComments(
            GetPostCommentsRequest(postId = postId)
        )
    }
}