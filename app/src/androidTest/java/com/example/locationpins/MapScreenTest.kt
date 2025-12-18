package com.example.locationpins.ui.screen.map

import android.Manifest
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.rule.GrantPermissionRule
import com.mapbox.maps.Style
import com.mapbox.search.result.SearchSuggestion
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class MapScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()


    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val mockViewModel = mockk<MapViewModel>(relaxed = true)

    private val uiStateFlow = MutableStateFlow(MapUiState())

    @Before
    fun setup() {
        MockKAnnotations.init(this)


        mockkObject(LocationManager)
        every { LocationManager.init(any()) } just Runs
        every { LocationManager.onPermissionGranted() } just Runs
        every { LocationManager.location } returns MutableStateFlow(null)

        every { mockViewModel.uiState } returns uiStateFlow
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testDefauldViewAndRemoteNodes() {
        uiStateFlow.value = MapUiState()

        composeTestRule.setContent {
            MapScreen(viewModel = mockViewModel)
        }


        composeTestRule.onRoot().printToLog("DEBUG_TREE")

        composeTestRule.onNodeWithText("Tìm kiếm địa điểm...").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Chọn kiểu bản đồ").assertIsDisplayed()
    }

    @Test
    fun testSearchStateAndClearNode() {
        uiStateFlow.value = MapUiState(query = "Cafe", isSearching = false)

        composeTestRule.setContent {
            MapScreen(viewModel = mockViewModel)
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Cafe").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Clear")
            .assertIsDisplayed()
            .performClick()

        verify { mockViewModel.onClearQuery() }
    }

    @Test
    fun testShowSuggestedList() {
        val mockSuggestion = mockk<SearchSuggestion>(relaxed = true)
        every { mockSuggestion.name } returns "Hồ Gươm"
        every { mockSuggestion.fullAddress } returns "Hoàn Kiếm, Hà Nội"

        uiStateFlow.value = MapUiState(
            query = "Hồ",
            suggestions = listOf(mockSuggestion),
            isSearching = false
        )

        composeTestRule.setContent {
            MapScreen(viewModel = mockViewModel)
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Hồ Gươm").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hoàn Kiếm, Hà Nội").assertIsDisplayed()
    }

    @Test
    fun testBottomSheetSelectTypeMap() {
        uiStateFlow.value = MapUiState(
            showBottomSheet = true,
            currentStyleUri = Style.MAPBOX_STREETS
        )

        composeTestRule.setContent {
            MapScreen(viewModel = mockViewModel)
        }


        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Chọn kiểu bản đồ").assertIsDisplayed()
        composeTestRule.onNodeWithText("Satellite").performClick()

        composeTestRule.waitForIdle()

        verify(timeout = 5000) {
            mockViewModel.onMapStyleSelected(any())
        }
    }
}