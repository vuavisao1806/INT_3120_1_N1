package com.example.locationpins

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.locationpins.ui.screen.gallery.GalleryScreen
import com.example.locationpins.ui.screen.gallery.GalleryUiState
import com.example.locationpins.ui.screen.gallery.GalleryViewModel
import com.example.locationpins.ui.screen.gallery.PinSummary
import com.example.locationpins.ui.screen.gallery.PostListView
import com.example.locationpins.ui.screen.gallery.PostSummary
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class GalleryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockViewModel = mockk<GalleryViewModel>(relaxed = true)
    private val uiStateFlow = MutableStateFlow(GalleryUiState())


    @Test
    fun showErrorWhenErrorExists() {
        val errorMessage = "Lỗi kết nối mạng"
        val errorState = GalleryUiState(isLoadingPin = false, error = errorMessage)
        every { mockViewModel.uiState } returns uiStateFlow
        uiStateFlow.value = errorState

        composeTestRule.setContent {
            GalleryScreen(
                viewModel = mockViewModel,
                onPinClick = {},
                onBackClick = {}
            )
        }

        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
        composeTestRule.onNodeWithText("Thử lại").assertIsDisplayed()
    }

    @Test
    fun showEmptyStateWhenNoPins() {
        val emptyState = GalleryUiState(
            isLoadingPin = false,
            error = null,
            pinSummaries = emptyList()
        )
        every { mockViewModel.uiState } returns uiStateFlow
        uiStateFlow.value = emptyState

        composeTestRule.setContent {
            GalleryScreen(
                viewModel = mockViewModel,
                onPinClick = {},
                onBackClick = {}
            )
        }

        composeTestRule.onNodeWithText("Chưa có ghim nào").assertIsDisplayed()
        composeTestRule.onNodeWithText("0 ghim").assertIsDisplayed()
    }

    @Test
    fun showPinsAndClickPin() {
        val pins = listOf(
            PinSummary(pinId = 1, coverImageUrl = "url1", postCount = 5),
            PinSummary(pinId = 2, coverImageUrl = "url2", postCount = 10)
        )
        val contentState = GalleryUiState(
            isLoadingPin = false,
            pinSummaries = pins
        )
        every { mockViewModel.uiState } returns uiStateFlow
        uiStateFlow.value = contentState

        var clickedPinId = -1

        composeTestRule.setContent {
            GalleryScreen(
                viewModel = mockViewModel,
                onPinClick = { clickedPinId = it },
                onBackClick = {}
            )
        }

        composeTestRule.onNodeWithText("2 ghim").assertIsDisplayed()

        composeTestRule.onNodeWithText("5").assertIsDisplayed()
        composeTestRule.onNodeWithText("10").assertIsDisplayed()

        composeTestRule.onNodeWithText("5").performClick()

        assert(clickedPinId == 1)
    }

    @Test
    fun showPostsCorrectly() {
        val pinId = 1
        val posts = listOf(
            PostSummary(postId = 101, imageUrl = "url_p1", reactionCount = 20, commentCount = 5)
        )

        val state = GalleryUiState(
            isLoadingPost = false,
            pinSummaries = listOf(PinSummary(1, "url", 10)),
            currentPinPosts = posts
        )

        every { mockViewModel.uiState } returns uiStateFlow
        every { mockViewModel.loadPostsForPin(any()) } returns Unit
        uiStateFlow.value = state

        composeTestRule.setContent {
            PostListView(
                pinId = pinId,
                onBackClick = {},
                onPostPress = {},
                viewModel = mockViewModel
            )
        }

        composeTestRule.onNodeWithText("1 bài đăng").assertIsDisplayed()
        composeTestRule.onNodeWithText("20").assertIsDisplayed()
        composeTestRule.onNodeWithText("5").assertIsDisplayed()
    }
}