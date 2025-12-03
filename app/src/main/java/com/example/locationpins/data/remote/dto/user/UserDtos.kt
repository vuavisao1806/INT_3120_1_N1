package com.example.locationpins.data.remote.dto.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    @SerialName("user_name")
    val userName: String,

    @SerialName("user_password")
    val userPassword: String,

    )

@Serializable
data class UserDto(
    @SerialName("user_id") val useId: Int,
    @SerialName("user_name") val useName: String,
    @SerialName("user_email") val useEmail: String,
    @SerialName("avatar_url") val avatarUrl: String,
    @SerialName("role") val role: String,
    @SerialName("quotes") val quotes: String,
    @SerialName("location") val location: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("name") val name: String,
    @SerialName("phone_num") val phoneNum: String,
    @SerialName("website") val website: String
)
@Serializable
data class LoginResponse(
    val success: Boolean,
    val user: UserDto?
)