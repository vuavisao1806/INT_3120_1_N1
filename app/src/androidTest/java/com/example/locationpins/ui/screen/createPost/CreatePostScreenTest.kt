package com.example.locationpins.ui.screen.createPost

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.example.locationpins.data.remote.dto.badge.CheckBadgesResponse
import com.example.locationpins.data.remote.dto.pins.PinByCoordResponse
import com.example.locationpins.data.remote.dto.post.InsertPostSuccess
import com.example.locationpins.data.remote.dto.post.UploadImageResponse
import com.example.locationpins.data.remote.dto.tag.GoogleLabelResponse
import com.example.locationpins.data.repository.BadgeRepository
import com.example.locationpins.data.repository.CreatePostRepository
import com.example.locationpins.data.repository.PinRepository
import com.example.locationpins.data.repository.PostRepository
import com.example.locationpins.data.repository.SensitiveContentRepository
import com.example.locationpins.data.repository.TagRepository
import com.example.locationpins.ui.screen.login.CurrentUser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CreatePostScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var createPostRepository: CreatePostRepository
    private lateinit var sensitiveContentRepository: SensitiveContentRepository
    private lateinit var postRepository: PostRepository
    private lateinit var pinRepository: PinRepository
    private lateinit var tagRepository: TagRepository
    private lateinit var badgeRepository: BadgeRepository

    private lateinit var fakeCreatePostViewModel: CreatePostViewModel

    @Before
    fun setUp() {
        createPostRepository = mockk<CreatePostRepository>()
        sensitiveContentRepository = mockk<SensitiveContentRepository>()
        postRepository = mockk<PostRepository>()
        pinRepository = mockk<PinRepository>()
        tagRepository = mockk<TagRepository>()
        badgeRepository = mockk<BadgeRepository>()

        fakeCreatePostViewModel = CreatePostViewModel(
            createPostRepository = createPostRepository,
            sensitiveContentRepository = sensitiveContentRepository,
            postRepository = postRepository,
            pinRepository = pinRepository,
            tagRepository = tagRepository,
            badgeRepository = badgeRepository
        )

        CurrentUser.currentUser = CreatePostMockData.sampleUser
    }

    @Test
    fun submit_normal_post() {
        val context = composeTestRule.activity
        val imageUri = createFakeImageUri(context)


        coEvery { sensitiveContentRepository.isSensitiveImage(any()) } returns false
        coEvery { sensitiveContentRepository.isSensitiveText(any()) } returns false

        coEvery { createPostRepository.uploadImage(any()) } returns UploadImageResponse(
            success = true,
            url = "http://fake/image.jpg",
            path = "http://fake/path"
        )

        coEvery { pinRepository.getPinIdByCoordinates(any(), any(), any()) } returns PinByCoordResponse(
            pinId = 197,
            isNewPin = true
        )

        coEvery {
            postRepository.insertPost(
                pinId = any(),
                userId = any(),
                title = any(),
                content = any(),
                imageUrl = any(),
                status = any()
            )
        } returns InsertPostSuccess(
            insertPostSuccess = true,
            postId = 1028
        )

        coEvery { tagRepository.getGoogleLabelsTopK(any(), k = 3) } returns GoogleLabelResponse(
            tags = listOf("food", "people", "amazing")
        )
        coEvery { tagRepository.assignTags(any(), any(), any()) } returns Unit

        coEvery { badgeRepository.checkAndAwardBadges(any()) } returns CheckBadgesResponse(
            newlyEarned = emptyList(),
            totalBadges = 0
        )

        var navigated: Boolean? = null

        composeTestRule.setContent {
            CreatePostScreen(
                initialImageUri = imageUri,
                onNavigateBack = { navigated = it },
                user = CreatePostMockData.sampleUser,
                viewModel = fakeCreatePostViewModel
            )
        }

        composeTestRule.onNodeWithTag("titleField").performTextInput("Chao buoi sang")
        composeTestRule.onNodeWithTag("contentField").performTextInput("Hanoi dep qua")
        composeTestRule.onNodeWithTag("submitButton").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) { navigated == true }

        composeTestRule.onNodeWithText("Đăng bài thành công")
            .assertIsDisplayed()

        coVerify(exactly = 1) { createPostRepository.uploadImage(any()) }
        coVerify(exactly = 1) { pinRepository.getPinIdByCoordinates(any(), any(), any()) }
        coVerify(exactly = 1) { postRepository.insertPost(any(), any(), any(), any(), any(), any()) }
        coVerify(exactly = 1) { tagRepository.getGoogleLabelsTopK(any(), k = 3) }
        coVerify(exactly = 1) { tagRepository.assignTags(postId = 1028, userId = any(), tags = any()) }
    }

    @Test
    fun submit_post_with_sensitive_image() {
        val context = composeTestRule.activity

        val imageUri = createFakeImageUri(context)

        coEvery { sensitiveContentRepository.isSensitiveImage(any()) } returns true
        coEvery { sensitiveContentRepository.isSensitiveText(any()) } returns false

        composeTestRule.setContent { 
            CreatePostScreen(
                initialImageUri = imageUri,
                onNavigateBack = {},
                viewModel = fakeCreatePostViewModel,
                user = CreatePostMockData.sampleUser
            )
        }

        composeTestRule.onNodeWithTag("titleField").performTextInput("Chao buoi sang")
        composeTestRule.onNodeWithTag("contentField").performTextInput("Hanoi dep qua")
        composeTestRule.onNodeWithTag("submitButton").performClick()

        composeTestRule.onNodeWithText("Hình ảnh chứa nội dung nhạy cảm. Không thể tải lên.")
            .assertIsDisplayed()

        coVerify(exactly = 0) { createPostRepository.uploadImage(any()) }
        coVerify(exactly = 0) { postRepository.insertPost(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun submit_post_with_sensitive_title() {
        val context = composeTestRule.activity

        val imageUri = createFakeImageUri(context)

        coEvery { sensitiveContentRepository.isSensitiveImage(any()) } returns false
        coEvery { sensitiveContentRepository.isSensitiveText(any()) } returns false
        coEvery { sensitiveContentRepository.isSensitiveText("Chao buoi sang") } returns true

        composeTestRule.setContent {
            CreatePostScreen(
                initialImageUri = imageUri,
                onNavigateBack = {},
                viewModel = fakeCreatePostViewModel,
                user = CreatePostMockData.sampleUser
            )
        }

        composeTestRule.onNodeWithTag("titleField").performTextInput("Chao buoi sang")
        composeTestRule.onNodeWithTag("contentField").performTextInput("Hanoi dep qua")
        composeTestRule.onNodeWithTag("submitButton").performClick()

        composeTestRule.onNodeWithText("Tiêu đề bài viết chứa nội dung nhạy cảm. Không thể tải lên.")
            .assertIsDisplayed()

        coVerify(exactly = 0) { createPostRepository.uploadImage(any()) }
        coVerify(exactly = 0) { postRepository.insertPost(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun submit_post_with_sensitive_content() {
        val context = composeTestRule.activity

        val imageUri = createFakeImageUri(context)

        coEvery { sensitiveContentRepository.isSensitiveImage(any()) } returns false
        coEvery { sensitiveContentRepository.isSensitiveText(any()) } returns false
        coEvery { sensitiveContentRepository.isSensitiveText("lmao hihi") } returns true

        composeTestRule.setContent {
            CreatePostScreen(
                initialImageUri = imageUri,
                onNavigateBack = {},
                viewModel = fakeCreatePostViewModel,
                user = CreatePostMockData.sampleUser
            )
        }

        composeTestRule.onNodeWithTag("titleField").performTextInput("Chao buoi sang")
        composeTestRule.onNodeWithTag("contentField").performTextInput("lmao hihi")
        composeTestRule.onNodeWithTag("submitButton").performClick()

        composeTestRule.onNodeWithText("Bài viết chứa nội dung nhạy cảm. Không thể tải lên.")
            .assertIsDisplayed()

        coVerify(exactly = 0) { createPostRepository.uploadImage(any()) }
        coVerify(exactly = 0) { postRepository.insertPost(any(), any(), any(), any(), any(), any()) }
    }

    private fun createFakeImageUri(context: Context): Uri {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "test_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        val uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        ) ?: error("Cannot create MediaStore uri")

        context.contentResolver.openOutputStream(uri)!!.use { out ->
            out.write(ByteArray(256) { 1 })
        }
        return uri
    }
}