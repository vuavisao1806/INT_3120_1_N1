package com.example.locationpins.ui.screen.pinDiscovery

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.Before

class PinDiscoveryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Mock ViewModel
    private lateinit var viewModel: PinDiscoveryViewModel
    // StateFlow giả để điều khiển trạng thái UI
    private val uiStateFlow = MutableStateFlow(PinDiscoveryUiState())

    @Before
    fun setup() {
        // Tạo mock cho ViewModel với relaxed = true để bỏ qua các hàm void không quan trọng
        viewModel = mockk(relaxed = true)

        // Gán stateFlow giả vào viewModel.uiState
        every { viewModel.uiState } returns uiStateFlow
    }

    @Test
    fun testInitialStateShowsDistanceSelection() {
        // GIVEN: Trạng thái ban đầu (GameState.Initial)
        uiStateFlow.value = PinDiscoveryUiState(gameState = GameState.Initial)

        // WHEN: Render màn hình
        composeTestRule.setContent {

            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                PinDiscoveryScreen(
                    onDismiss = {},
                    onPinFound = {},
                    viewModel = viewModel
                )
            }
        }

        // THEN: Kiểm tra các thành phần của màn hình chọn khoảng cách
        composeTestRule.onNodeWithText("Khám phá Ghim Gần Đây").assertIsDisplayed()
        composeTestRule.onNodeWithText("Chọn khoảng cách để bắt đầu cuộc phiêu lưu!").performScrollTo().assertIsDisplayed()

        // Kiểm tra xem các nút khoảng cách có hiển thị không
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
        // GIVEN: Trạng thái đang tìm kiếm (GameState.Searching)
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

        // THEN: Kiểm tra hiển thị hint bubble (Card chứa hint)

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
        // GIVEN: Trạng thái đã tìm thấy (GameState.Found)
        uiStateFlow.value = PinDiscoveryUiState(gameState = GameState.Found)

        composeTestRule.setContent {
            PinDiscoveryScreen(
                onDismiss = {},
                onPinFound = {},
                viewModel = viewModel
            )
        }

        // THEN: Kiểm tra thông báo chúc mừng
        composeTestRule.onNodeWithText("Chúc Mừng!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bạn đã tìm thấy ghim ẩn!").assertIsDisplayed()

        // Kiểm tra nút chức năng
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

        // Action: Click Chơi Lại
        composeTestRule.onNodeWithText("Chơi Lại").performClick()

        // Verify: Hàm resetGame() được gọi
        verify { viewModel.resetGame() }
    }
}