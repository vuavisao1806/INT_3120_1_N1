package com.example.locationpins.ui.screen.postDetail

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.locationpins.data.remote.dto.comment.CommentDto
import com.example.locationpins.data.remote.dto.post.PostDto
import com.example.locationpins.data.remote.dto.tag.TagDto
import com.example.locationpins.data.repository.BadgeRepository
import com.example.locationpins.data.repository.CreatePostRepository
import com.example.locationpins.data.repository.PinRepository
import com.example.locationpins.data.repository.PostRepository
import com.example.locationpins.data.repository.SensitiveContentRepository
import com.example.locationpins.data.repository.TagRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// ==========================================
// MOCK DATA (Dữ liệu giả lập)
// ==========================================

val mockPostFull = PostDto(
    pinId = 1,
    status = "published",
    title = "Chuyến đi tuyệt vời",
    postId = 1,
    userId = 101,
    userName = "Người dùng Test",
    avatarUrl = "https://example.com/avatar.jpg",
    imageUrl = "https://example.com/post_image.jpg",
    body = "Nội dung bài viết mẫu để test UI. Hôm nay trời rất đẹp!",
    createdAt = "Vừa xong",
    reactionCount = 100,
    commentCount = 50
)

val mockTags = listOf(
    TagDto(tagId = 1, name = "#Travel"),
    TagDto(tagId = 2, name = "#Vietnam")
)

val mockComments = listOf(
    CommentDto(
        commentId = 10,
        userId = 202,
        userName = "Commenter A",
        avatarUrl = "",
        content = "Bài viết hay quá!",
        createdAt = "1 phút trước",
        postId = 1
    ),
    CommentDto(
        commentId = 11,
        userId = 203,
        userName = "Commenter B",
        avatarUrl = "",
        content = "Tuyệt vời!",
        createdAt = "2 phút trước",
        postId = 1
    )
)

// ==========================================
// CLASS TEST CHÍNH
// ==========================================

class PostDetailScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    // ==========================================
    // NHÓM 1: KIỂM TRA TRẠNG THÁI HIỂN THỊ (STATE)
    // ==========================================

    @Test
    fun state_Loading_ShowsProgressIndicator() {
        // Given: State đang loading (Mặc định isLoading = true)
        val state = PostDetailUiState(isLoading = true)

        // When
        setContentWithState(state)

        // Then
        composeTestRule.onNodeWithTag(PostDetailTestTags.LOADING).assertIsDisplayed()
        composeTestRule.onNodeWithTag(PostDetailTestTags.POST_CONTENT).assertDoesNotExist()
        composeTestRule.onNodeWithTag(PostDetailTestTags.ERROR_TEXT).assertDoesNotExist()
    }

    @Test
    fun state_Error_ShowsMessageAndRetryButton() {
        // Given: State bị lỗi, đã tắt loading
        val errorMsg = "Mất kết nối internet"
        val state = PostDetailUiState(
            isLoading = false, // Quan trọng: Phải tắt loading
            error = errorMsg,
            post = null
        )

        // When
        setContentWithState(state)

        // Then
        composeTestRule.onNodeWithTag(PostDetailTestTags.LOADING).assertDoesNotExist()
        composeTestRule.onNodeWithTag(PostDetailTestTags.ERROR_TEXT)
            .assertIsDisplayed()
            .assertTextContains(errorMsg)
        composeTestRule.onNodeWithTag(PostDetailTestTags.RETRY_BUTTON)
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun state_Success_ShowsFullPostContent() {
        // Given: State thành công
        val state = PostDetailUiState(
            isLoading = false, // Quan trọng: Phải tắt loading
            post = mockPostFull,
            tags = mockTags,
            comments = mockComments
        )

        // When
        setContentWithState(state)

        // Then
        // 1. Kiểm tra list chính
        composeTestRule.onNodeWithTag(PostDetailTestTags.POST_CONTENT).assertIsDisplayed()

        // 2. Kiểm tra thông tin Header
        composeTestRule.onNodeWithText("Người dùng Test").assertIsDisplayed()
        composeTestRule.onNodeWithText("Vừa xong").assertIsDisplayed()

        // 3. Kiểm tra nội dung body
        composeTestRule.onNodeWithText("Nội dung bài viết mẫu để test UI. Hôm nay trời rất đẹp!").assertIsDisplayed()

        // 4. Kiểm tra Tags có hiện
        composeTestRule.onNodeWithText("#Travel").assertIsDisplayed()
        composeTestRule.onNodeWithText("#Vietnam").assertIsDisplayed()

        // 5. Kiểm tra số lượng like/comment
        composeTestRule.onNodeWithText("100", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("50", substring = true).assertIsDisplayed()
    }

    @Test
    fun state_Success_ShowsCommentsList() {
        val state = PostDetailUiState(
            isLoading = false, // Quan trọng
            post = mockPostFull,
            comments = mockComments
        )

        setContentWithState(state)

        // Kiểm tra comment xuất hiện
        composeTestRule.onNodeWithText("Commenter A").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bài viết hay quá!").assertIsDisplayed()

        // Đếm số lượng comment item
        composeTestRule.onAllNodesWithTag(PostDetailTestTags.COMMENT_ITEM).assertCountEquals(2)
    }

    // ==========================================
    // NHÓM 2: KIỂM TRA TƯƠNG TÁC (INTERACTION)
    // ==========================================

    @Test
    fun action_ClickRetry_TriggersCallback() {
        var isRetryClicked = false
        val state = PostDetailUiState(
            isLoading = false,
            error = "Error",
            post = null
        )

        setContentWithState(state, onRetry = { isRetryClicked = true })

        composeTestRule.onNodeWithTag(PostDetailTestTags.RETRY_BUTTON).performClick()

        assert(isRetryClicked) { "Nút thử lại không kích hoạt callback" }
    }

    @Test
    fun action_ClickLike_TriggersCallback() {
        var isLikeClicked = false
        val state = PostDetailUiState(
            isLoading = false,
            post = mockPostFull,
            isLiked = false
        )

        setContentWithState(state, onLikeClick = { isLikeClicked = true })

        composeTestRule.onNodeWithTag(PostDetailTestTags.LIKE_BUTTON).performClick()

        assert(isLikeClicked)
    }

    @Test
    fun action_TypeComment_UpdatesCallback() {
        var currentInput = ""
        val state = PostDetailUiState(
            isLoading = false,
            post = mockPostFull
        )

        setContentWithState(state, onCommentTextChange = { currentInput = it })

        composeTestRule.onNodeWithTag(PostDetailTestTags.COMMENT_INPUT)
            .performTextInput("Hello World")

        assert(currentInput == "Hello World")
    }

    // ==========================================
    // NHÓM 3: LOGIC UI (ENABLE/DISABLE/STATE CHANGE)
    // ==========================================

    @Test
    fun logic_LikeButton_ChangeIconDescription_WhenLiked() {
        // Case: Chưa Like
        val unlikedState = PostDetailUiState(
            isLoading = false,
            post = mockPostFull,
            isLiked = false
        )
        setContentWithState(unlikedState)

        composeTestRule.onNodeWithContentDescription("Like").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Unlike").assertDoesNotExist()
    }

    @Test
    fun logic_LikeButton_ShowsUnlike_WhenLiked() {
        // Case: Đã Like
        val likedState = PostDetailUiState(
            isLoading = false,
            post = mockPostFull,
            isLiked = true
        )
        setContentWithState(likedState)

        composeTestRule.onNodeWithContentDescription("Unlike").assertIsDisplayed()
    }

    @Test
    fun logic_SendButton_Disabled_WhenTextEmpty() {
        // Case: Không có chữ -> Disable
        val state = PostDetailUiState(
            isLoading = false,
            post = mockPostFull,
            commentText = ""
        )

        setContentWithState(state)

        composeTestRule.onNodeWithTag(PostDetailTestTags.SEND_BUTTON)
            .assertIsNotEnabled()
    }

    @Test
    fun logic_SendButton_Enabled_WhenTextExists() {
        // Case: Có chữ -> Enable
        val state = PostDetailUiState(
            isLoading = false,
            post = mockPostFull,
            commentText = "Có nội dung"
        )

        setContentWithState(state)

        composeTestRule.onNodeWithTag(PostDetailTestTags.SEND_BUTTON)
            .assertIsEnabled()
    }

    @Test
    fun logic_SendButton_ShowsLoading_WhenSubmitting() {
        // Case: Có chữ + Đang gửi -> Disable + Hiện Loading
        val state = PostDetailUiState(
            isLoading = false, // <-- QUAN TRỌNG: Phải set false để hiện giao diện
            post = mockPostFull,
            commentText = "ABC", // <-- Giả lập đã nhập chữ
            isSubmittingComment = true // <-- Đang gửi
        )

        setContentWithState(state)

        // 1. Nút Send phải hiện hữu nhưng bị disable
        composeTestRule.onNodeWithTag(PostDetailTestTags.SEND_BUTTON).assertIsNotEnabled()

        // 2. Icon Send (mũi tên) KHÔNG được hiện
        composeTestRule.onNodeWithContentDescription("Gửi").assertDoesNotExist()

        // 3. Nếu bạn đã thêm tag "Small_Loading" vào CircularProgressIndicator nhỏ trong file UI
        // thì bỏ comment dòng dưới để test:
        // composeTestRule.onNodeWithTag("Small_Loading").assertIsDisplayed()
    }

    @Test
    fun logic_SendComment_TriggersAction() {
        var isSent = false
        val state = PostDetailUiState(
            isLoading = false,
            post = mockPostFull,
            commentText = "OK"
        )

        setContentWithState(state, onSendComment = { isSent = true })

        composeTestRule.onNodeWithTag(PostDetailTestTags.SEND_BUTTON).performClick()

        assert(isSent)
    }

    // ==========================================
    // HELPER FUNCTION
    // ==========================================
    private fun setContentWithState(
        state: PostDetailUiState,
        currentUserAvatar: String = "avatar_url",
        onClickUserName: (Int) -> Unit = {},
        onRetry: () -> Unit = {},
        onLikeClick: () -> Unit = {},
        onDeleteComment: (Int) -> Unit = {},
        onCommentTextChange: (String) -> Unit = {},
        onSendComment: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            PostDetailContent(
                uiState = state,
                currentUserAvatar = currentUserAvatar,
                onClickUserName = onClickUserName,
                onRetry = onRetry,
                onLikeClick = onLikeClick,
                onDeleteComment = onDeleteComment,
                onCommentTextChange = onCommentTextChange,
                onSendComment = onSendComment
            )
        }
    }
}