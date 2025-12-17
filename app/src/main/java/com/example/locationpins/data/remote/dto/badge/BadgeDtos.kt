package com.example.locationpins.data.remote.dto.badge

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetUserBadgesRequest(
    @SerialName("user_id")
    val userId: Int
)

@Serializable
data class BadgeDto(
    @SerialName("badge_id")
    val badgeId: Int,
    val name: String,
    val description: String,
    @SerialName("icon_name")
    val iconName: String,
    val tier: String,
    @SerialName("earned_at")
    val earnedAt: String? = null,
    @SerialName("is_earned")
    val isEarned: Boolean = false
)

@Serializable
data class GetEarnedBadgesRequest(
    @SerialName("user_id")
    val userId: Int,
    val limit: Int = 3
)

@Serializable
data class CheckAndAwardBadgesRequest(
    @SerialName("user_id")
    val userId: Int
)

@Serializable
data class NewlyEarnedBadgeDto(
    @SerialName("badge_id")
    val badgeId: Int,
    val name: String,
    val description: String,
    @SerialName("icon_name")
    val iconName: String,
    val tier: String
)

@Serializable
data class CheckBadgesResponse(
    @SerialName("newly_earned")
    val newlyEarned: List<NewlyEarnedBadgeDto>,
    @SerialName("total_badges")
    val totalBadges: Int
)

@Serializable
data class GetBadgeProgressRequest(
    @SerialName("user_id")
    val userId: Int
)

@Serializable
data class BadgeProgressDto(
    @SerialName("badge_id")
    val badgeId: Int,
    val name: String,
    val description: String,
    @SerialName("icon_name")
    val iconName: String,
    val tier: String,
    @SerialName("requirement_type")
    val requirementType: String,
    @SerialName("requirement_value")
    val requirementValue: Int,
    @SerialName("current_value")
    val currentValue: Int,
    @SerialName("progress_percentage")
    val progressPercentage: Float,
    @SerialName("is_earned")
    val isEarned: Boolean
)