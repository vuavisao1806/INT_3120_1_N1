package com.example.locationpins.ui.screen.createPost

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationpins.data.repository.BadgeRepository
import com.example.locationpins.data.repository.CreatePostRepository
import com.example.locationpins.data.repository.PinRepository
import com.example.locationpins.data.repository.PostRepository
import com.example.locationpins.data.repository.SensitiveContentRepository
import com.example.locationpins.data.repository.TagRepository
import com.example.locationpins.ui.screen.login.CurrentUser
import com.example.locationpins.ui.screen.map.LocationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class CreatePostViewModel(
    private val createPostRepository: CreatePostRepository = CreatePostRepository(),
    private val sensitiveContentRepository: SensitiveContentRepository = SensitiveContentRepository(),
    private val postRepository: PostRepository = PostRepository(),
    private val pinRepository: PinRepository = PinRepository(),
    private val tagRepository: TagRepository = TagRepository(),
    private val badgeRepository: BadgeRepository = BadgeRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePostUiState())
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()

    init {
        // Load post location
        loadPostLocation()
    }

    fun loadPostLocation() {
        val currentLocation = LocationManager.location.value
        _uiState.update {
            it.copy(
                centerLatitude = currentLocation!!.latitude,
                centerLongitude = currentLocation.longitude
            )
        }
    }

    fun submitPost(
        context: Context,
        userId: Int,
        title: String,
        content: String,
        imageUri: Uri,
        status: String = "active",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // 1. Convert Uri -> Multipart
                val imagePart = uriToMultipart(context, imageUri, "file")

                val (_isSensitiveImage, _isSensitiveTitle, _isSensitiveContent) = supervisorScope {
                    val isSensitiveImage = async(Dispatchers.IO) {
                        runCatching { sensitiveContentRepository.isSensitiveImage(imagePart) }
                    }
                    val isSensitiveTitle = async(Dispatchers.IO) {
                        runCatching { sensitiveContentRepository.isSensitiveText(text = title) }
                    }
                    val isSensitiveContent = async(Dispatchers.IO) {
                        runCatching { sensitiveContentRepository.isSensitiveText(text = content) }
                    }
                    Triple(isSensitiveImage.await(), isSensitiveTitle.await(), isSensitiveContent.await())
                }

                // 1.1. Check sensitive image
                val isSensitiveImage = _isSensitiveImage.getOrElse { error ->
                    onError("Lỗi khi kiểm tra nội dung hình ảnh: ${error.message}")
                    return@launch
                }
                if (isSensitiveImage) {
                    onError("Hình ảnh chứa nội dung nhạy cảm. Không thể tải lên.")
                    return@launch
                }

                // 1.2. Check sensitive text
                val isSensitiveTitle = _isSensitiveTitle.getOrElse { error ->
                    onError("Lỗi khi kiểm tra nội dung tiêu đề bài viết: ${error.message}")
                    return@launch
                }
                if (isSensitiveTitle) {
                    onError("Tiêu đề bài viết chứa nội dung nhạy cảm. Không thể tải lên.")
                    return@launch
                }

                val isSensitiveContent = _isSensitiveContent.getOrElse { error ->
                    onError("Lỗi khi kiểm tra nội dung bài viết: ${error.message}")
                    return@launch
                }
                if (isSensitiveContent) {
                    onError("Bài viết chứa nội dung nhạy cảm. Không thể tải lên.")
                    return@launch
                }

                // 2. Upload ảnh
                val uploadRes = createPostRepository.uploadImage(imagePart)
                if (!uploadRes.success) {
                    onError("Upload ảnh thất bại")
                    return@launch
                }

                val imageUrl = uploadRes.url

                // 3. Gọi /posts/insert
                val (insertRes, labelRes) = supervisorScope {
                    val insertRes = async {
                        postRepository.insertPost(
                            pinId = getSelfPinIdByCoordinates(),
                            userId = userId,
                            title = title,
                            content = content,
                            imageUrl = imageUrl,
                            status = status
                        )
                    }

                    val labelRes = async {
                        runCatching { tagRepository.getGoogleLabelsTopK(imagePart, k = 3) }
                    }

                    Pair(insertRes.await(), labelRes.await())
                }

                if (!insertRes.insertPostSuccess) {
                    onError("Tạo bài đăng thất bại")
                    return@launch
                }

//                val labelRes = tagRepository.getGoogleLabelsTopK(imagePart, k = 3)
                val tags = labelRes.getOrNull()?.tags.orEmpty()

                // (4) gửi tags lên backend để insert vào 3 bảng
                if (tags.isNotEmpty()) {
                    tagRepository.assignTags(
                        postId = insertRes.postId,
                        userId = userId,
                        tags = tags
                    )
                }

//                badgeRepository.checkAndAwardBadges(CurrentUser.currentUser!!.userId)

                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Có lỗi xảy ra")
            }
        }
    }

    suspend fun getSelfPinIdByCoordinates(): Int {
        // We set up radiusMeters as default value (50m)
        return pinRepository.getPinIdByCoordinates(
            centerLatitude = _uiState.value.centerLatitude,
            centerLongitude = _uiState.value.centerLongitude
        ).pinId
    }

    private fun uriToMultipart(
        context: Context,
        uri: Uri,
        partName: String
    ): MultipartBody.Part {
        val contentResolver = context.contentResolver

        val inputStream = contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Không đọc được dữ liệu từ Uri")

        val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
        tempFile.outputStream().use { output ->
            inputStream.copyTo(output)
        }

        val mimeType = contentResolver.getType(uri) ?: "image/*"
        val requestBody = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())

        return MultipartBody.Part.createFormData(
            partName,
            tempFile.name,
            requestBody
        )
    }
}
