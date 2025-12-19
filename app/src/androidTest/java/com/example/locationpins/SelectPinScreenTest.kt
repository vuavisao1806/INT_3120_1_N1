package com.example.locationpins

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.locationpins.data.remote.dto.post.PostDto
import com.example.locationpins.ui.screen.map.SelectedPinScreen
import com.example.locationpins.ui.screen.map.SelectedPinUiState
import com.example.locationpins.ui.screen.map.SelectedPinViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class SelectedPinScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeUiState = MutableStateFlow(SelectedPinUiState())

    private val mockViewModel = mockk<SelectedPinViewModel>(relaxed = true) {
        every { uiState } returns fakeUiState
    }

    @Test
    fun testLoadingState() {
        fakeUiState.value = SelectedPinUiState(isLoading = true)

        composeTestRule.setContent {
            SelectedPinScreen(
                pinId = 1,
                viewModel = mockViewModel,
                onBackClick = {},
                onPostPress = {}
            )
        }

        composeTestRule.onNodeWithText("Thử lại").assertDoesNotExist()
    }

    @Test
    fun testErrorStateAndRetry() {
        fakeUiState.value = SelectedPinUiState(
            isLoading = false,
            errorMessage = "Lỗi kết nối server"
        )

        composeTestRule.setContent {
            SelectedPinScreen(
                pinId = 1,
                viewModel = mockViewModel,
                onBackClick = {},
                onPostPress = {}
            )
        }


        composeTestRule.onNodeWithText("Lỗi kết nối server").assertIsDisplayed()


        composeTestRule.onNodeWithText("Thử lại").performClick()

        verify { mockViewModel.loadPostsByPinId(1) }
    }

    @Test
    fun testDataSuccessDisplaysPostCounts() {
        val mockPosts = listOf(
            PostDto(
                postId = 101,
                pinId = 1,
                title = "Cà phê sáng",
                body = "Quán view đẹp, đồ uống ngon.",
                imageUrl = "https://example.com/img1.jpg",
                userId = 99,
                userName = "User A",
                avatarUrl = "https://example.com/avatarA.jpg",
                status = "PUBLIC",
                reactionCount = 150,
                commentCount = 25,
                createdAt = "2024-01-01"
            ),
            PostDto(
                postId = 102,
                pinId = 1,
                title = "Check-in",
                body = "Hơi đông nhưng vui.",
                imageUrl = "https://example.com/img2.jpg",
                userId = 100,
                userName = "User B",
                avatarUrl = "https://example.com/avatarB.jpg",
                status = "PUBLIC",
                reactionCount = 10,
                commentCount = 2,
                createdAt = "2024-01-02"
            )
        )


        fakeUiState.value = SelectedPinUiState(posts = mockPosts)

        composeTestRule.setContent {
            SelectedPinScreen(
                pinId = 1,
                viewModel = mockViewModel,
                onBackClick = {},
                onPostPress = {}
            )
        }



        composeTestRule.onNodeWithText("150").assertIsDisplayed()
        composeTestRule.onNodeWithText("25").assertIsDisplayed()

        composeTestRule.onNodeWithText("10").assertIsDisplayed()
        composeTestRule.onNodeWithText("2").assertIsDisplayed()
    }
}