package com.example.locationpins.data.repository

import com.example.locationpins.data.remote.RetrofitClient
import com.example.locationpins.data.remote.dto.user.CheckIsFriendRequest
import com.example.locationpins.data.remote.dto.user.GetUserRequest
import com.example.locationpins.data.remote.dto.user.LoginRequest
import com.example.locationpins.data.remote.dto.user.LoginResponse
import com.example.locationpins.data.remote.dto.user.RegisterRequest
import com.example.locationpins.data.remote.dto.user.RegisterResponse
import com.example.locationpins.data.remote.dto.user.RespondRequest
import com.example.locationpins.data.remote.dto.user.RespondResponse
import com.example.locationpins.data.remote.dto.user.SendContactRequest
import com.example.locationpins.data.remote.dto.user.SendContactResult
import com.example.locationpins.data.remote.dto.user.ShowContactRequest
import com.example.locationpins.data.remote.dto.user.ShowContactRespond
import com.example.locationpins.data.remote.dto.user.UpdateProfileRequest
import com.example.locationpins.data.remote.dto.user.UpdateProfileResponse
import com.example.locationpins.data.remote.dto.user.UserDto
import com.mapbox.base.common.logger.model.Message

// This is extremely bad code but whatever
const val DEFAULT_AVATAR_URL =
    "https://dwadscpiphluqvkwrgdf.supabase.co/storage/v1/object/public/avatars/empty_avatar.png"

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
        return api.register(
            RegisterRequest(
                userName = username,
                name = name,
                userEmail = userEmail,
                userPassword = userPassword,
                avatarUrl = avatarUrl
            )
        )
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

    suspend fun getUser(currentUserId: Int, gotUserId: Int): UserDto {
        return api.getUser(
            GetUserRequest(currentUserId = currentUserId, gotUserId = gotUserId)
        )
    }

    suspend fun showContactRequest(userId: Int): List<ShowContactRespond> {
        return api.showContactRequest(ShowContactRequest(userId))
    }


    suspend fun respondContact(ownId: Int, otherId: Int, isAccept: Boolean): RespondResponse {
        return api.respondContact(RespondRequest(ownId, otherId, isAccept))
    }

    suspend fun updateProfile(
        userId: Int,
        name: String,
        quotes: String,
        avatarUrl: String?,
        location: String,
        userEmail: String,
        website: String
    ): UpdateProfileResponse {
        return api.updateProfile(
            UpdateProfileRequest(
                userId,
                name,
                quotes,
                avatarUrl,
                location,
                userEmail,
                website
            )
        )
    }

    suspend fun sendContact(
        followingUserId: Int,
        followedUserId: Int,
        message: String = ""
    ): SendContactResult {
        return api.sendContact(SendContactRequest(followingUserId, followedUserId, message))
    }
}