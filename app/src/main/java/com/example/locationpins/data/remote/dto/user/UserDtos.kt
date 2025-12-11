package com.example.locationpins.data.remote.dto.user

import com.example.locationpins.R
import com.example.locationpins.data.model.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// This is extremely bad code but whatever
const val DEFAULT_AVATAR_URL = "https://dwadscpiphluqvkwrgdf.supabase.co/storage/v1/object/public/avatars/default_avt.jpg"

@Serializable
data class LoginRequest(
    @SerialName("user_name")
    val userName: String,

    @SerialName("user_password")
    val userPassword: String,

    )

@Serializable
data class UserDto(
    @SerialName("user_id") val userId: Int,


    @SerialName("user_name") val username: String,


    @SerialName("user_email") val userEmail: String,

    @SerialName("avatar_url") val avatarUrl: String,

    @SerialName("quotes") val quotes: String? = null,

    @SerialName("location") val location: String? = null,

    @SerialName("name") val name: String,

    @SerialName("phone_num") val phoneNumber: String? = null,

    @SerialName("website") val website: String? = null
)

fun UserDto.toUser(): User {
    return User(
        userId = userId,
        userName = username,
        location = location,
        avatarUrl = avatarUrl,
        quote = quotes,
        name = name,
        quantityPin = 0, // TODO: fix this
        quantityReact = 0, // TODO: fix this
        quantityComment = 0, // TODO: fix this
        quantityContact = 0, // TODO: fix this
        userEmail = userEmail,
        phoneNumber = phoneNumber,
        website = website
    )
}

@Serializable
data class LoginResponse(
    val success: Boolean,
    val user: UserDto?
)