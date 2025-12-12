package com.example.locationpins.ui.screen.createPost

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationpins.data.repository.CreatePostRepository
import com.example.locationpins.data.repository.PinRepository
import com.example.locationpins.data.repository.PostRepository
import com.example.locationpins.data.repository.SensitiveContentRepository
import com.example.locationpins.ui.screen.map.LocationManager
import com.example.locationpins.ui.screen.newfeed.NewsFeedUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class CreatePostViewModel(
    private val createPostRepository: CreatePostRepository = CreatePostRepository(),
    private val sensitiveContentRepository: SensitiveContentRepository = SensitiveContentRepository(),
    private val postRepository: PostRepository = PostRepository(),
    private val pinRepository: PinRepository = PinRepository()
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

                // 1.1. Check sensitive image
                try {
                    val isSensitive = sensitiveContentRepository.isSensitiveImage(imagePart)

                    if (isSensitive) {
                        onError("Hình ảnh chứa nội dung nhạy cảm. Không thể tải lên.")
                        return@launch
                    }

                } catch (e: Exception) {
                    onError("Lỗi khi kiểm tra nội dung hình ảnh: ${e.message}")
                    return@launch
                }

                // 1.2. Check sensitive text
                try {
                    val isSensitive: Boolean = sensitiveContentRepository.isSensitiveText(text = title)
                    if (isSensitive) {
                        Log.d("SENSITIVE DETECTION", "The content on the title isn't allowed")
                        onError("Tiêu đề bài viết chứa nội dung nhạy cảm. Không thể tải lên.")
                        return@launch
                    }
                } catch (e: Exception) {
                    onError("Lỗi khi kiểm tra nội dung tiêu đề bài viết: ${e.message}")
                    return@launch
                }

                try {
                    val isSensitive: Boolean = sensitiveContentRepository.isSensitiveText(text = content)
                    if (isSensitive) {
                        Log.d("SENSITIVE DETECTION", "The content isn't allowed")
                        onError("Bài viết chứa nội dung nhạy cảm. Không thể tải lên.")
                        return@launch
                    }
                } catch (e: Exception) {
                    onError("Lỗi khi kiểm tra nội dung bài viết: ${e.message}")
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
                val insertRes = postRepository.insertPost(
                    pinId = getSelfPinIdByCoordinates(),
                    userId = userId,
                    title = title,
                    content = content,
                    imageUrl = imageUrl,
                    status = status
                )
                if (!insertRes) {
                    onError("Tạo bài đăng thất bại")
                    return@launch
                }

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
