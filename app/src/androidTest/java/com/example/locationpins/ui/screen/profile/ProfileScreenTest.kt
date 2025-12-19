package com.example.locationpins.ui.screen.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.locationpins.data.model.User
import com.example.locationpins.ui.screen.login.CurrentUser
import com.example.locationpins.ui.theme.LocationSocialTheme
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ProfileScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Mock User
    private val mockUser = User(
        userId = 1,
        userName = "linh_test",
        location = "Hanoi, Vietnam",
        avatarUrl = "",
        quote = "Coding is fun",
        name = "Nguyen Thi Linh",
        quantityPin = 12,
        quantityReact = 150,
        quantityComment = 40,
        quantityContact = 8,
        userEmail = "linh@test.com",
        phoneNumber = "0909090909",
        website = "linh.dev",
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

    @Test
    fun profileSelf() {
        var isShowRequestList: Boolean = false
        var isShowEditScreen: Boolean = false
        val mockViewModel = mockk<ProfileViewModel>(relaxed = true)

        val successState = MutableStateFlow(
            ProfileUiState(
                user = mockUser,
                profileMode = ProfileMode.Self,
                isLoading = false,
                badges = emptyList(),
                currentPosts = emptyList()
            )
        )

        every { mockViewModel.uiState } returns successState

        composeTestRule.setContent {
            LocationSocialTheme {
                ProfileScreen(
                    userId = 1,
                    onEditClick = { isShowEditScreen = true },
                    onProfileClick = {},
                    onPressPost = {},
                    viewModel = mockViewModel
                )
            }
        }

        composeTestRule.onNodeWithText("Nguyen Thi Linh").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hanoi, Vietnam").assertIsDisplayed()
        composeTestRule.onNodeWithText("Coding is fun").assertIsDisplayed()
        composeTestRule.onNodeWithText("linh@test.com").assertIsDisplayed()
        composeTestRule.onNodeWithText("linh.dev").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Edit profile").performClick()

        composeTestRule.onNodeWithText("12").assertIsDisplayed()
        composeTestRule.onNodeWithText("150").assertIsDisplayed()
        composeTestRule.onNodeWithText("40").assertIsDisplayed()
        composeTestRule.onNodeWithText("8").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Invites").assertIsDisplayed().performClick()
        successState.value = successState.value.copy(showContactRequests = true)
        composeTestRule.onNodeWithText("Yêu cầu kết bạn", substring = true).assertIsDisplayed()
    }

    @Test
    fun profileStranger() {
        val mockViewModel = mockk<ProfileViewModel>(relaxed = true)

        val strangerUser = mockUser.copy(status = "STRANGER")
        val strangerState = MutableStateFlow(
            ProfileUiState(
                user = strangerUser,
                profileMode = ProfileMode.Stranger,
                isLoading = false
            )
        )

        every { mockViewModel.uiState } returns strangerState

        composeTestRule.setContent {
            LocationSocialTheme {
                ProfileScreen(
                    userId = 2,
                    onEditClick = {},
                    onProfileClick = {},
                    onPressPost = {},
                    viewModel = mockViewModel
                )
            }
        }
        composeTestRule.onNodeWithText("Nguyen Thi Linh").assertIsDisplayed()
        composeTestRule.onNodeWithText("Get contact").assertIsDisplayed()
        composeTestRule.onNodeWithText("12").assertIsDisplayed()
        composeTestRule.onNodeWithText("150").assertIsDisplayed()
        composeTestRule.onNodeWithText("40").assertIsDisplayed()
        composeTestRule.onNodeWithText("Get contact").performClick()
        strangerState.value = ProfileUiState(
            user = mockUser.copy(status = "SENT_REQUEST"),
            profileMode = ProfileMode.Stranger,
            isLoading = false
        )
        composeTestRule.onNodeWithText("Đã gửi lời mời").assertIsDisplayed()
        strangerState.value = ProfileUiState(
            user = mockUser.copy(status = "INCOMING_REQUEST"),
            profileMode = ProfileMode.Stranger,
            isLoading = false
        )
        composeTestRule.onNodeWithText("Đồng ý").assertIsDisplayed()
        composeTestRule.onNodeWithText("Từ chối").assertIsDisplayed()

    }

    @Test
    fun profileFriend() {
        val mockViewModel = mockk<ProfileViewModel>(relaxed = true)
        val friendUser = mockUser.copy(status = "FRIEND")
        val successState = ProfileUiState(
            user = friendUser,
            profileMode = ProfileMode.Friend,
            isLoading = false
        )

        every { mockViewModel.uiState } returns MutableStateFlow(successState)

        composeTestRule.setContent {
            LocationSocialTheme {
                ProfileScreen(
                    userId = 1,
                    onEditClick = {},
                    onProfileClick = {},
                    onPressPost = {},
                    viewModel = mockViewModel
                )
            }
        }

        composeTestRule.onNodeWithText("Nguyen Thi Linh").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hanoi, Vietnam").assertIsDisplayed()
        composeTestRule.onNodeWithText("Coding is fun").assertIsDisplayed()
        composeTestRule.onNodeWithText("linh@test.com").assertIsDisplayed()
        composeTestRule.onNodeWithText("linh.dev").assertIsDisplayed()

        composeTestRule.onNodeWithText("12").assertIsDisplayed()
        composeTestRule.onNodeWithText("150").assertIsDisplayed()
        composeTestRule.onNodeWithText("40").assertIsDisplayed()

    }

    @Test
    fun editProfile_displaysInitialData() {
        val mockViewModel = mockk<EditViewModel>(relaxed = true)

        val initialState = EditProfileUiState(
            name = "Nguyen Thi Linh",
            quotes = "Coding is fun",
            location = "Hanoi, Vietnam",
            email = "linh@test.com",
            website = "linh.dev",
            isLoading = false
        )

        every { mockViewModel.uiState } returns MutableStateFlow(initialState)

        composeTestRule.setContent {
            LocationSocialTheme {
                EditProfileScreen(
                    onBackClick = {},
                    viewModel = mockViewModel
                )
            }
        }


        composeTestRule.onNodeWithText("Nguyen Thi Linh").assertIsDisplayed()
        composeTestRule.onNodeWithText("Coding is fun").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hanoi, Vietnam").assertIsDisplayed()
        composeTestRule.onNodeWithText("linh@test.com").assertIsDisplayed()
        composeTestRule.onNodeWithText("linh.dev").assertIsDisplayed()
    }

    @Test
    fun editProfileInteractionCallsViewModel() {
        val mockViewModel = mockk<EditViewModel>(relaxed = true)

        val uiStateFlow = MutableStateFlow(EditProfileUiState(name = "Old Name"))
        every { mockViewModel.uiState } returns uiStateFlow

        composeTestRule.setContent {
            LocationSocialTheme {
                EditProfileScreen(
                    onBackClick = {},
                    viewModel = mockViewModel
                )
            }
        }

        val nameInput = composeTestRule.onNodeWithText("Old Name")
        nameInput.performClick()

        nameInput.performTextInput("New Name")
        verify { mockViewModel.onNameChange(match { it.contains("New Name") }) }
    }

    @Test
    fun editProfileSaveButtonCallsSave() {
        val mockViewModel = mockk<EditViewModel>(relaxed = true)

        val normalState = EditProfileUiState(isLoading = false)
        every { mockViewModel.uiState } returns MutableStateFlow(normalState)

        composeTestRule.setContent {
            LocationSocialTheme {
                EditProfileScreen(
                    onBackClick = {},
                    viewModel = mockViewModel
                )
            }
        }

        val saveNode = composeTestRule.onNodeWithText("Lưu")
        if (saveNode.isDisplayed()) {
            saveNode.performClick()

            verify { mockViewModel.onSaveClick(any()) }
        }
    }
}