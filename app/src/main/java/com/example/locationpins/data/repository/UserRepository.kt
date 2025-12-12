package com.example.locationpins.data.repository

import com.example.locationpins.data.remote.RetrofitClient
import com.example.locationpins.data.remote.dto.react.CheckPostReactRequest
import com.example.locationpins.data.remote.dto.user.CheckIsFriendRequest
import com.example.locationpins.data.remote.dto.user.LoginRequest
import com.example.locationpins.data.remote.dto.user.LoginResponse
import com.example.locationpins.data.remote.dto.user.RegisterRequest
import com.example.locationpins.data.remote.dto.user.RegisterResponse

// This is extremely bad code but whatever
const val DEFAULT_AVATAR_URL = "https://dwadscpiphluqvkwrgdf.supabase.co/storage/v1/object/public/avatars/empty_avatar.png"

class UserRepository {
    private val api = RetrofitClient.api

    suspend fun login(
        userName: String,
        userPassword: String
    ): LoginResponse {
        return api.login(LoginRequest(userName = userName, userPassword = userPassword))
    }

    suspend fun register(
        username: String,
        name: String,
        userPassword: String,
        userEmail: String,
        avatarUrl: String = DEFAULT_AVATAR_URL
    ): RegisterResponse {
        return api.register(RegisterRequest(
            userName = username,
            name = name,
            userEmail = userEmail,
            userPassword = userPassword,
            avatarUrl = avatarUrl
        ))
    }

    suspend fun isFriend(
        ownId: Int,
        otherId: Int
    ): Boolean {
        return api.checkIsFriend(
            CheckIsFriendRequest(
                ownId = ownId,
                otherId = otherId
            )
        ).isFriend
    }
}