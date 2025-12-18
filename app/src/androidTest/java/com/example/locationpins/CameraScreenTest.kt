package com.example.locationpins

import android.Manifest
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.rule.GrantPermissionRule
import com.example.locationpins.ui.screen.camera.CameraCaptureScreen
import com.example.locationpins.ui.screen.camera.DemoCameraScreen
import org.junit.Rule
import org.junit.Test

class CameraScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    @Test
    fun testDemoCameraScreenFlow() {
        composeTestRule.setContent {
            DemoCameraScreen()
        }

        composeTestRule.onNodeWithText("Demo Camera Screen").assertIsDisplayed()

        composeTestRule.onAllNodes(isFocusable()).onFirst().performClick()

        composeTestRule.waitForIdle() // Chờ UI cập nhật
        composeTestRule.onNodeWithContentDescription("Đóng").assertExists()
        composeTestRule.onNodeWithContentDescription("Chụp ảnh").assertExists()
    }

    @Test
    fun testCameraCaptureScreenUIElements() {
        var isCancelled = false

        composeTestRule.setContent {
            CameraCaptureScreen(
                onImageCaptured = { },
                onCancel = { isCancelled = true }
            )
        }


        composeTestRule.onNodeWithContentDescription("Đóng").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Chụp ảnh").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Đổi camera").assertIsDisplayed()




        composeTestRule.onNodeWithContentDescription("Đóng").performClick()
        assert(isCancelled) { "Callback onCancel chưa được gọi" }
    }


    @Test
    fun testCameraLoadingState() {
        composeTestRule.setContent {
            CameraCaptureScreen(
                onImageCaptured = {},
                onCancel = {}
            )
        }

        composeTestRule.onNodeWithText("Đang định vị...").assertExists()
    }
}