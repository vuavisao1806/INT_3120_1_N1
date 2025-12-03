package com.example.locationpins.data.remote

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

fun uriToMultipart(
    context: Context,
    uri: Uri,
    partName: String = "file"
): MultipartBody.Part {
    val contentResolver = context.contentResolver

    // Tạo file tạm để gửi
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
