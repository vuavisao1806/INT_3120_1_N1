package com.example.locationpins.data.remote.dto.user

import com.example.locationpins.data.model.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.Serial

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
    @SerialName("website") val website: String? = null,
    @SerialName("total_pin") val quantityPin: Int = 0,
    @SerialName("total_reaction") val quantityReact: Int = 0,
    @SerialName("total_comment") val quantityComment: Int = 0,
    @SerialName("total_contact") val quantityContact: Int = 0,
    @SerialName("relationship_status") val status: String = "SELF"
)

fun UserDto.toUser(): User {
    return User(
        userId = userId,
        userName = username,
        location = location,
        avatarUrl = avatarUrl,
        quote = quotes,
        name = name,
        quantityPin = quantityPin,
        quantityReact = quantityReact,
        quantityComment = quantityComment,
        quantityContact = quantityContact,
        userEmail = userEmail,
        phoneNumber = phoneNumber,
        website = website,
        status=status
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

@Serializable
data class CheckIsFriendRequest(
    @SerialName("own_id")
    val ownId: Int,
    @SerialName("other_id")
    val otherId: Int
)

@Serializable
data class IsFriendRespond(
    @SerialName("is_friend")
    val isFriend: Boolean
)

@Serializable
data class GetUserRequest(
    @SerialName("current_user_id")
    val currentUserId: Int,
    @SerialName("got_user_id")
    val gotUserId:Int
)

@Serializable
data class ShowContactRequest(
    @SerialName("user_id") val userId: Int
)

@Serializable
data class ShowContactRespond(
    @SerialName("user_id") val followingUserId: Int,
    @SerialName("user_name") val userName: String,
    @SerialName("created_at") val timeCreate: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("status") var status: String = "PENDING",
    @SerialName("message") val message: String?
)

@Serializable
data class RespondRequest(
    @SerialName("own_id")
    val ownId: Int,
    @SerialName("other_id")
    val otherId: Int,
    @SerialName("isAccept")
    val isAccept: Boolean
)

@Serializable
data class RespondResponse(
    @SerialName("is_success") val isSuccess: Boolean
)


@Serializable
data class UpdateProfileRequest(

    @SerialName("user_id") val userId: Int,

    @SerialName("name") val name: String,

    @SerialName("quotes") val quotes: String?,

    @SerialName("avatar_url") val avatarUrl: String?,

    @SerialName("location") val location: String?,

    @SerialName("email") val email: String?,

    @SerialName("website") val website: String?
)

@Serializable
data class UpdateProfileResponse(
    @SerialName("update_user_by_user_id_success") val success: Boolean = true
)

@Serializable
data class SendContactRequest(
    @SerialName("following_user_id") val followingUserId: Int,
    @SerialName("followed_user_id") val followedUserId: Int,
    @SerialName("message") val message: String = ""
)

@Serializable
data class SendContactResult(
    @SerialName("is_success") val isSuccess: Boolean = true
)

@Serializable
data class GetPostByUserRequest(
    @SerialName("user_id") val userId:Int
)