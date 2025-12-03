package com.example.locationpins.ui.screen.createPost

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.locationpins.data.remote.ApiService
import com.example.locationpins.data.remote.dto.post.InsertPostRequest
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class CreatePostViewModel(
    private val apiService: ApiService
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

                // 2. Upload ảnh
                val uploadRes = apiService.uploadImage(imagePart)
                if (!uploadRes.success) {
                    onError("Upload ảnh thất bại")
                    return@launch
                }

                val imageUrl = uploadRes.url

                // 3. Gọi /posts/insert
                val req = InsertPostRequest(
                    pin_id = pinId,
                    user_id = userId,
                    title = title,
                    body = content,
                    image_url = imageUrl,
                    status = status
                )

                val insertRes = apiService.insertPost(req)
                if (!insertRes.insert_post_success) {
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

class CreatePostViewModelFactory(
    private val apiService: ApiService
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreatePostViewModel::class.java)) {
            return CreatePostViewModel(apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
