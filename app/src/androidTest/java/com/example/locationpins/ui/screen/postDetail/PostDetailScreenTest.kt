package com.example.locationpins.ui.screen.postDetail

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.locationpins.data.remote.dto.comment.CommentDto
import com.example.locationpins.data.remote.dto.post.PostDto
import com.example.locationpins.data.remote.dto.tag.TagDto
import org.junit.Rule
import org.junit.Test

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
        postId = 1,
        childOfCommentId = null
    ),
    CommentDto(
        commentId = 11,
        userId = 203,
        userName = "Commenter B",
        avatarUrl = "",
        content = "Tuyệt vời!",
        createdAt = "2 phút trước",
        postId = 1,
        childOfCommentId = null
    )
)

class PostDetailScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun state_Loading_ShowsProgressIndicator() {
        val state = PostDetailUiState(isLoading = true)

        setContentWithState(state)

        composeTestRule.onNodeWithTag(PostDetailTestTags.LOADING).assertIsDisplayed()
        composeTestRule.onNodeWithTag(PostDetailTestTags.POST_CONTENT).assertDoesNotExist()
        composeTestRule.onNodeWithTag(PostDetailTestTags.ERROR_TEXT).assertDoesNotExist()
    }

    @Test
    fun state_Error_ShowsMessageAndRetryButton() {
        val errorMessage = "Mất kết nối internet"
        val state = PostDetailUiState(
            isLoading = false, // Quan trọng: Phải tắt loading
            error = errorMessage,
            post = null
        )

        setContentWithState(state)

        composeTestRule.onNodeWithTag(PostDetailTestTags.LOADING).assertDoesNotExist()
        composeTestRule.onNodeWithTag(PostDetailTestTags.ERROR_TEXT)
            .assertIsDisplayed()
            .assertTextContains(errorMessage)
        composeTestRule.onNodeWithTag(PostDetailTestTags.RETRY_BUTTON)
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun state_Success_ShowsFullPostContent() {
        val state = PostDetailUiState(
            isLoading = false,
            post = mockPostFull,
            tags = mockTags,
            comments = mockComments
        )

        setContentWithState(state)

        composeTestRule.onNodeWithTag(PostDetailTestTags.POST_CONTENT).assertIsDisplayed()

        composeTestRule.onNodeWithText("Người dùng Test").assertIsDisplayed()
        composeTestRule.onNodeWithText("Vừa xong").assertIsDisplayed()

        composeTestRule.onNodeWithText("Nội dung bài viết mẫu để test UI. Hôm nay trời rất đẹp!").assertIsDisplayed()

        composeTestRule.onNodeWithText("#Travel").assertIsDisplayed()
        composeTestRule.onNodeWithText("#Vietnam").assertIsDisplayed()

        composeTestRule.onNodeWithText("100", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("50", substring = true).assertIsDisplayed()
    }

    @Test
    fun state_Success_ShowsCommentsList() {
        val state = PostDetailUiState(
            isLoading = false,
            post = mockPostFull,
            comments = mockComments
        )

        setContentWithState(state)

        composeTestRule.onNodeWithText("Commenter A").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bài viết hay quá!").assertIsDisplayed()

        composeTestRule.onAllNodesWithTag(PostDetailTestTags.COMMENT_ITEM).assertCountEquals(2)
    }

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

    @Test
    fun send_sensitive_comment() {
        var currentInput = ""
        val state = PostDetailUiState(
            isLoading = false,
            post = mockPostFull
        )

        setContentWithState(state, onCommentTextChange = { currentInput = it })

        composeTestRule.onNodeWithTag(PostDetailTestTags.COMMENT_INPUT)
            .performTextInput("Hello World")
    }


    @Test
    fun logic_LikeButton_ChangeIconDescription_WhenLiked() {
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
        val state = PostDetailUiState(
            isLoading = false,
            post = mockPostFull,
            commentText = "ABC",
            isSubmittingComment = true
        )

        setContentWithState(state)

        composeTestRule.onNodeWithTag(PostDetailTestTags.SEND_BUTTON).assertIsNotEnabled()

        composeTestRule.onNodeWithContentDescription("Gửi").assertDoesNotExist()
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
                onSendComment = onSendComment,
                onNavigateBack = {},
                onTagPress = {},
                onReplyCommentClick = {},
                onReplyCommentCancel = {},
                onExpandCommentClick = {}
            )
        }
    }

    @Test
    fun showsErrorSnackbar_whenSensitiveContentDetected() {

        val sensitiveErrorState = PostDetailUiState(
            isLoading = false,
            post = mockPostFull,
            error = "Bình luận chứa nội dung nhạy cảm"
        )


        composeTestRule.setContent {
            PostDetailContent(
                uiState = sensitiveErrorState,
                currentUserAvatar = "",
                onNavigateBack = {},
                onClickUserName = {},
                onRetry = {},
                onLikeClick = {},
                onReplyCommentClick = {},
                onReplyCommentCancel = {},
                onDeleteComment = {},
                onCommentTextChange = {},
                onSendComment = {},
                onExpandCommentClick = {}
            )
        }


        composeTestRule
            .onNodeWithText("Bình luận chứa nội dung nhạy cảm")
            .assertIsDisplayed()
    }
}