package com.example.locationpins.data.remote.dto.user

import com.example.locationpins.data.model.User
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

@Serializable
data class RegisterRequest(
    @SerialName("user_name")
    val userName: String,
    val name: String,
    @SerialName("user_email")
    val userEmail: String,
    @SerialName("user_password")
    val userPassword: String,
    @SerialName("avatar_url")
    val avatarUrl: String
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