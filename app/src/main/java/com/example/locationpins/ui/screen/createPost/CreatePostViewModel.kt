package com.example.locationpins.ui.screen.createPost

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationpins.data.repository.CreatePostRepository
import com.example.locationpins.data.repository.PostRepository
import com.example.locationpins.data.repository.SensitiveContentRepository
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class CreatePostViewModel(
//    private val apiService: ApiService // TODO: where is the corresponding repository?
    private val createPostRepository: CreatePostRepository = CreatePostRepository(),
    private val sensitiveContentRepository: SensitiveContentRepository = SensitiveContentRepository(),
    private val postRepository: PostRepository = PostRepository()
) : ViewModel() {

    fun submitPost(
        context: Context,
        pinId: Int,
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
                    pinId = pinId,
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

//class CreatePostViewModelFactory(
//    private val apiService: ApiService
//) : ViewModelProvider.Factory {
//    @Suppress("UNCHECKED_CAST")
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(CreatePostViewModel::class.java)) {
//            return CreatePostViewModel(apiService) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}
