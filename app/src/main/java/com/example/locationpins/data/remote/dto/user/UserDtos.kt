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


    @SerialName("user_email") val useEmail: String? = null,

    @SerialName("avatar_url") val avatarUrl: String? = null,

    @SerialName("quotes") val quotes: String? = null,

    @SerialName("location") val location: String? = null,

    @SerialName("name") val name: String? = null,

    @SerialName("phone_num") val phoneNum: String? = null,

    @SerialName("website") val website: String? = null
)

@Serializable
data class LoginResponse(
    val success: Boolean,
    val user: UserDto?
)

@Serializable
data class RegisterRequest(
    @SerialName("user_name")
    val userName: String,
    @SerialName("user_email")
    val userEmail: String,
    @SerialName("user_password")
    val userPassword: String
)


@Serializable
data class RegisterResponse(
    @SerialName("user_name_taken")
    val userNameTaken: Boolean? = null,

    @SerialName("user_email_taken")
    val userEmailTaken: Boolean? = null,

    @SerialName("register_success")
    val registerSuccess: Boolean? = null
)