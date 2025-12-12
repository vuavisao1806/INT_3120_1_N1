package com.example.locationpins.data.repository

import com.example.locationpins.data.remote.RetrofitClient
import com.example.locationpins.data.remote.dto.post.SensitiveTextRespond
import com.example.locationpins.data.remote.dto.sensitive.CheckIsSensitiveText
import okhttp3.MultipartBody

class SensitiveContentRepository {
    private val api = RetrofitClient.api

    suspend fun isSensitiveText(
        text: String
    ): Boolean {
        return api.checkIsSensitiveText(
            CheckIsSensitiveText(
                text = text,
            )
        ).isSensitive
    }

    suspend fun isSensitiveImage(imagePart: MultipartBody.Part): SensitiveTextRespond {
        return api.checkIsSensitiveImage(imagePart)
    }
}
