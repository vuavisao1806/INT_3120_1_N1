package com.example.locationpins.ui.screen.newfeed

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import com.example.locationpins.data.model.Post
import com.example.locationpins.data.model.User
import com.example.locationpins.ui.screen.login.CurrentUser
import com.example.locationpins.ui.theme.LocationSocialTheme
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NewFeedScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    // Mock User
    private val mockUser = User(
        userId = 1,
        userName = "test_user",
        location = "Vietnam",
        avatarUrl = "",
        quote = "",
        name = "Test User",
        quantityPin = 0,
        quantityReact = 0,
        quantityComment = 0,
        quantityContact = 0,
        userEmail = "test@email.com",
        phoneNumber = "",
        website = "",
        status = "SELF"
    )

    @Before
    fun setup() {
        CurrentUser.currentUser = mockUser
    }

    @After
    fun tearDown() {
        CurrentUser.currentUser = null
    }

    // --- CASE 1: LOADING ---
    @Test
    fun newFeedLoadingStateShowsProgressIndicator() {
        val mockViewModel = mockk<NewsFeedViewModel>(relaxed = true)

        val loadingState = NewsFeedUiState(
            isLoading = true,
            posts = emptyList()
        )
        every { mockViewModel.uiState } returns MutableStateFlow(loadingState)

        composeTestRule.setContent {
            LocationSocialTheme {
                NewsFeedScreen(viewModel = mockViewModel)
            }
        }

        composeTestRule.onNodeWithText("Không có bài viết nào").assertDoesNotExist()
    }

    // --- CASE 2: EMPTY STATE  ---
    @Test
    fun newFeedEmptyStateShowsNoPostsMessage() {
        val mockViewModel = mockk<NewsFeedViewModel>(relaxed = true)

        val emptyState = NewsFeedUiState(
            isLoading = false,
            posts = emptyList(),
            error = null,
            filterTag = "TestTag"
        )
        every { mockViewModel.uiState } returns MutableStateFlow(emptyState)

        composeTestRule.setContent {
            LocationSocialTheme {
                NewsFeedScreen(viewModel = mockViewModel)
            }
        }


        composeTestRule.onNodeWithText("Không có bài viết nào").assertIsDisplayed()
        composeTestRule.onNodeWithText("🔍").assertIsDisplayed()
    }

    // --- CASE 3: CONTENT SHOWS POSTS (Có scroll) ---
    @Test
    fun newFeedContentStateShowsPosts() {
        val mockViewModel = mockk<NewsFeedViewModel>(relaxed = true)

        val post1 =  Post(
            postId = 1,
            pinId = 1,
            title = "Cầu Vàng Đà Nẵng lúc hoàng hôn",
            body = "Đứng trên Cầu Vàng...",
            imageUrl = "https://example.com/image1.jpg",
            reactCount = 100,
            commentCount = 50,
            tags = listOf("Đà Nẵng", "Travel")
        )

        val post2 =  Post(
            postId = 2,
            pinId = 2,
            title = "Phở bò Hà Nội sáng sớm",
            body = "Không gì đánh bại được...",
            imageUrl = "https://example.com/image2.jpg",
            reactCount = 200,
            commentCount = 30,
            tags = listOf("Hà Nội", "Food")
        )

        val contentState = NewsFeedUiState(
            isLoading = false,
            posts = listOf(post1, post2)
        )
        every { mockViewModel.uiState } returns MutableStateFlow(contentState)

        composeTestRule.setContent {
            LocationSocialTheme {
                NewsFeedScreen(viewModel = mockViewModel)
            }
        }


        composeTestRule.onNodeWithText("Cầu Vàng Đà Nẵng lúc hoàng hôn", substring = true)
            .assertIsDisplayed()


        composeTestRule.onNode(hasScrollAction())
            .performScrollToNode(
                hasText("Phở bò Hà Nội sáng sớm", substring = true)
            )

        composeTestRule.onNodeWithText("Phở bò Hà Nội sáng sớm", substring = true)
            .assertIsDisplayed()
    }

    // --- CASE 4: ERROR ---
    @Test
    fun newFeedErrorStateShowsErrorMessage() {
        val mockViewModel = mockk<NewsFeedViewModel>(relaxed = true)

        val errorState = NewsFeedUiState(
            isLoading = false,
            posts = emptyList(),
            error = "Lỗi kết nối mạng"
        )
        every { mockViewModel.uiState } returns MutableStateFlow(errorState)

        composeTestRule.setContent {
            LocationSocialTheme {
                NewsFeedScreen(viewModel = mockViewModel)
            }
        }

        composeTestRule.onNodeWithText("Lỗi kết nối mạng").assertIsDisplayed()
    }
}