package com.example.locationpins.ui.screen.pinDiscovery

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PinDiscoveryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var viewModel: PinDiscoveryViewModel
    private val uiStateFlow = MutableStateFlow(PinDiscoveryUiState())

    @Before
    fun setup() {
        viewModel = mockk(relaxed = true)

        every { viewModel.uiState } returns uiStateFlow
    }

    @Test
    fun testInitialStateShowsDistanceSelection() {
        uiStateFlow.value = PinDiscoveryUiState(gameState = GameState.Initial)

        composeTestRule.setContent {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                PinDiscoveryScreen(
                    onDismiss = {},
                    onPinFound = {},
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.onNodeWithText("Khám phá Ghim Gần Đây").assertIsDisplayed()
        composeTestRule.onNodeWithText("Chọn khoảng cách để bắt đầu cuộc phiêu lưu!").performScrollTo().assertIsDisplayed()

        composeTestRule.onNodeWithText("100m").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Bắt đầu").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun testDistanceSelectionUpdatesViewModel() {
        uiStateFlow.value = PinDiscoveryUiState(gameState = GameState.Initial, selectedDistance = 100)

        composeTestRule.setContent {
            PinDiscoveryScreen(
                onDismiss = {},
                onPinFound = {},
                viewModel = viewModel
            )
        }


        composeTestRule.onNodeWithText("200m").performClick()


        verify { viewModel.selectDistance(200) }
    }

    @Test
    fun testStartGameCallsViewModel() {
        uiStateFlow.value = PinDiscoveryUiState(gameState = GameState.Initial)

        composeTestRule.setContent {

            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                PinDiscoveryScreen(
                    onDismiss = {},
                    onPinFound = {},
                    viewModel = viewModel
                )
            }
        }


        composeTestRule.onNodeWithText("Bắt đầu")
            .performScrollTo()
            .performClick()

        verify { viewModel.startGame() }
    }

    @Test
    fun testSearchingStateShowsCompassAndHint() {
        val testHint = "Đi về phía trước 50m"
        uiStateFlow.value = PinDiscoveryUiState(
            gameState = GameState.Searching,
            lastHint = testHint
        )

        composeTestRule.setContent {
            PinDiscoveryScreen(
                onDismiss = {},
                onPinFound = {},
                viewModel = viewModel
            )
        }

        composeTestRule.waitUntil(timeoutMillis = 1000) {
            try {
                composeTestRule.onNodeWithText(testHint).assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }


    }

    @Test
    fun testFoundStateShowsSuccessMessage() {
        uiStateFlow.value = PinDiscoveryUiState(gameState = GameState.Found)

        composeTestRule.setContent {
            PinDiscoveryScreen(
                onDismiss = {},
                onPinFound = {},
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Chúc Mừng!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bạn đã tìm thấy ghim ẩn!").assertIsDisplayed()

        composeTestRule.onNodeWithText("Xem Ghim").assertIsDisplayed()
        composeTestRule.onNodeWithText("Chơi Lại").assertIsDisplayed()
    }

    @Test
    fun testFoundStateClickPlayAgainResetsGame() {
        uiStateFlow.value = PinDiscoveryUiState(gameState = GameState.Found)

        composeTestRule.setContent {
            PinDiscoveryScreen(
                onDismiss = {},
                onPinFound = {},
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Chơi Lại").performClick()

        verify { viewModel.resetGame() }
    }
}