package com.example.locationpins.data.remote


import com.example.locationpins.data.remote.dto.comment.CancelCommentRequest
import com.example.locationpins.data.remote.dto.comment.CommentDto
import com.example.locationpins.data.remote.dto.comment.CreateCommentRequest
import com.example.locationpins.data.remote.dto.comment.GetPostCommentsRequest
import com.example.locationpins.data.remote.dto.pins.FindRandomPinRequest
import com.example.locationpins.data.remote.dto.pins.GetPinListByUserIdRequest
import com.example.locationpins.data.remote.dto.pins.GetPinListInRadiusRequest
import com.example.locationpins.data.remote.dto.pins.PinByCoordRequest
import com.example.locationpins.data.remote.dto.pins.PinByCoordResponse
import com.example.locationpins.data.remote.dto.pins.PinDto
import com.example.locationpins.data.remote.dto.pins.RandomPinResponse
import com.example.locationpins.data.remote.dto.post.GetNewsfeedRequest
import com.example.locationpins.data.remote.dto.post.GetPinPreviewRequest
import com.example.locationpins.data.remote.dto.post.GetPostByPinRequest
import com.example.locationpins.data.remote.dto.post.GetPostRequest
import com.example.locationpins.data.remote.dto.post.InsertPostRequest
import com.example.locationpins.data.remote.dto.post.InsertPostSuccess
import com.example.locationpins.data.remote.dto.post.PinPreview
import com.example.locationpins.data.remote.dto.post.PostByPinResponse
import com.example.locationpins.data.remote.dto.post.PostDto
import com.example.locationpins.data.remote.dto.post.SensitiveTextRespond
import com.example.locationpins.data.remote.dto.post.UploadImageResponse
import com.example.locationpins.data.remote.dto.react.CancelReactionRequest
import com.example.locationpins.data.remote.dto.react.CheckPostReactRequest
import com.example.locationpins.data.remote.dto.react.ReactionRequest
import com.example.locationpins.data.remote.dto.tag.GetPostTagsRequest
import com.example.locationpins.data.remote.dto.tag.TagDto
import com.example.locationpins.data.remote.dto.react.CheckPostReactResponse
import com.example.locationpins.data.remote.dto.sensitive.CheckIsSensitiveText
import com.example.locationpins.data.remote.dto.sensitive.IsSensitiveTextRespond
import com.example.locationpins.data.remote.dto.tag.AssignTagsRequest
import com.example.locationpins.data.remote.dto.tag.GoogleLabelResponse
import com.example.locationpins.data.remote.dto.tag.UserFavoriteTagsRequest
import com.example.locationpins.data.remote.dto.user.CheckIsFriendRequest
import com.example.locationpins.data.remote.dto.user.GetUserRequest
import com.example.locationpins.data.remote.dto.user.IsFriendRespond
import com.example.locationpins.data.remote.dto.user.LoginRequest
import com.example.locationpins.data.remote.dto.user.LoginResponse
import com.example.locationpins.data.remote.dto.user.RegisterRequest
import com.example.locationpins.data.remote.dto.user.RegisterResponse
import com.example.locationpins.data.remote.dto.user.RespondRequest
import com.example.locationpins.data.remote.dto.user.RespondResponse
import com.example.locationpins.data.remote.dto.user.SendContactRequest
import com.example.locationpins.data.remote.dto.user.SendContactResult
import com.example.locationpins.data.remote.dto.user.ShowContactRequest
import com.example.locationpins.data.remote.dto.user.ShowContactRespond
import com.example.locationpins.data.remote.dto.user.UpdateProfileRequest
import com.example.locationpins.data.remote.dto.user.UpdateProfileResponse
import com.example.locationpins.data.remote.dto.user.UserDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

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

    @POST("/users/login")
    suspend fun login(
        @Body body: LoginRequest
    ): LoginResponse

    @POST("/posts/newsfeed")
    suspend fun getNewsfeed(
        @Body body: GetNewsfeedRequest
    ): List<PostDto>

    @Multipart
    @POST("/posts/upload-image")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part
    ): UploadImageResponse

    @POST("/posts/insert")
    suspend fun insertPost(
        @Body body: InsertPostRequest
    ): InsertPostSuccess

    @POST("posts/pinpreview")
    suspend fun getPinPreview(
        @Body request: GetPinPreviewRequest
    ): List<PinPreview>

    @POST("/posts/pinId")
    suspend fun getPostByPin(
        @Body request: GetPostByPinRequest
    ): List<PostByPinResponse>

    @POST("/users/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): RegisterResponse

    @POST("/posts/react/check")
    suspend fun checkPostReact(
        @Body request: CheckPostReactRequest
    ): CheckPostReactResponse

    @POST("/users/isfriend")
    suspend fun checkIsFriend(
        @Body request: CheckIsFriendRequest
    ): IsFriendRespond

    @POST("/users/get")
    suspend fun getUser(
        @Body request: GetUserRequest
    ): UserDto

    @POST("/sensitive/text")
    suspend fun checkIsSensitiveText(
        @Body request: CheckIsSensitiveText
    ): IsSensitiveTextRespond

    @Multipart
    @POST("/sensitive/image")
    suspend fun checkIsSensitiveImage(
        @Part file: MultipartBody.Part // Chờ nhận trực tiếp MultipartBody.Part
    ): SensitiveTextRespond

    @Multipart
    @POST("/tag/label_top3")
    suspend fun getGoogleLabelsTopK(
        @Part file: MultipartBody.Part,
        @Query("k") k: Int = 3
    ): GoogleLabelResponse


    @POST("/tag/assign")
    suspend fun assignTags(
        @Body req: AssignTagsRequest
    ): Unit

    @POST("/pins/get-or-create-by-coord")
    suspend fun getPinIdByCoordinates(
        @Body request: PinByCoordRequest
    ): PinByCoordResponse

    @POST("/pins/find-random")
    suspend fun findRandomPin(
        @Body body: FindRandomPinRequest
    ): RandomPinResponse


    @POST("/users/contact_request")
    suspend fun showContactRequest(
        @Body body: ShowContactRequest
    ): List<ShowContactRespond>

    @POST("/users/respond_contact")
    suspend fun respondContact(
        @Body body: RespondRequest
    ): RespondResponse

    @POST("/users/update")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): UpdateProfileResponse

    @POST("/users/send_contact")
    suspend fun sendContact(
        @Body request: SendContactRequest
    ): SendContactResult
    @POST("/users/tags")
    suspend fun getFavoriteTagsByUserId(
        @Body body: UserFavoriteTagsRequest
    ): Set<TagDto>
}


