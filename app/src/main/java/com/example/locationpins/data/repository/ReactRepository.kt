package com.example.locationpins.data.repository

import com.example.locationpins.data.remote.RetrofitClient
import com.example.locationpins.data.remote.dto.comment.CheckPostCommentRequest
import com.example.locationpins.data.remote.dto.react.CancelReactionRequest
import com.example.locationpins.data.remote.dto.react.ReactionRequest

class ReactionRepository {

    private val api = RetrofitClient.api

    suspend fun reactPost(
        postId: Int,
        userId: Int
    ) {
        api.reactPost(
            ReactionRequest(
                postId = postId,
                userId = userId
            )
        )
    }

    suspend fun cancelReactPost(
        postId: Int,
        userId: Int
    ) {
        api.cancelReactPost(
            CancelReactionRequest(
                postId = postId,
                userId = userId
            )
        )
    }

    suspend fun checkReactPost(
        postId: Int,
        userId: Int
    ): Boolean {
        return api.checkPostComment(
            CheckPostCommentRequest(
                postId = postId,
                userId = userId
            )
        ).haveReaction
    }
}






