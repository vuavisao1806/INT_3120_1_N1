package com.example.locationpins.data.remote


import com.example.locationpins.data.remote.dto.comment.CancelCommentRequest
import com.example.locationpins.data.remote.dto.comment.CommentDto
import com.example.locationpins.data.remote.dto.comment.CreateCommentRequest
import com.example.locationpins.data.remote.dto.comment.GetPostCommentsRequest
import com.example.locationpins.data.remote.dto.pins.PinDto
import com.example.locationpins.data.remote.dto.pins.GetPinListByUserIdRequest
import com.example.locationpins.data.remote.dto.pins.GetPinListInRadiusRequest
import com.example.locationpins.data.remote.dto.post.GetPostRequest
import com.example.locationpins.data.remote.dto.post.PostDto
import com.example.locationpins.data.remote.dto.react.CancelReactionRequest
import com.example.locationpins.data.remote.dto.react.ReactionRequest
import com.example.locationpins.data.remote.dto.tag.GetPostTagsRequest
import com.example.locationpins.data.remote.dto.tag.TagDto
import com.example.locationpins.data.remote.dto.post.GetNewsfeedRequest


import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("/pins/get/user-id")
    suspend fun getPinsByUserId(
        @Body body: GetPinListByUserIdRequest
    ): List<PinDto>

    @POST("/pins/get/in-radius")
    suspend fun getPinsByInRadius(
        @Body body: GetPinListInRadiusRequest
    ): List<PinDto>

    @POST("/posts/get")
    suspend fun getPost(
        @Body body: GetPostRequest
    ): PostDto

    @POST("/posts/get/comments")
    suspend fun getPostComments(
        @Body body: GetPostCommentsRequest
    ): List<CommentDto>

    @POST("/posts/comment")
    suspend fun createComment(
        @Body body: CreateCommentRequest
    ): Unit

    @POST("/posts/comment/cancel")
    suspend fun cancelComment(
        @Body body: CancelCommentRequest
    ): Unit

    @POST("/posts/react")
    suspend fun reactPost(
        @Body body: ReactionRequest
    ): Unit

    @POST("/posts/react/cancel")
    suspend fun cancelReactPost(
        @Body body: CancelReactionRequest
    ): Unit

    @POST("/posts/get/tags")
    suspend fun getPostTags(
        @Body body: GetPostTagsRequest
    ): List<TagDto>

    @POST("/posts/newsfeed")
    suspend fun getNewsfeed(
        @Body body: GetNewsfeedRequest
    ): List<PostDto>
}

