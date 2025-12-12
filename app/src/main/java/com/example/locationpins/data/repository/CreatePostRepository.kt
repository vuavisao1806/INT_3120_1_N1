package com.example.locationpins.data.repository

import com.example.locationpins.data.remote.RetrofitClient
import com.example.locationpins.data.remote.dto.post.UploadImageResponse
import okhttp3.MultipartBody

class CreatePostRepository {
    private val api = RetrofitClient.api

    suspend fun uploadImage(
        file: MultipartBody.Part
    ): UploadImageResponse {
        return api.uploadImage(file)
    }
}