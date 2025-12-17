package com.example.locationpins.data.model

data class Badge(
    val badgeId: Int,
    val name: String,
    val description: String,
    val iconName: String,
    val tier: String,
    val earnedAt: String? = null,
    val isEarned: Boolean = false
)

data class BadgeProgress(
    val badgeId: Int,
    val name: String,
    val description: String,
    val iconName: String,
    val tier: String,
    val requirementType: String,
    val requirementValue: Int,
    val currentValue: Int,
    val progressPercentage: Float,
    val isEarned: Boolean
)

data class NewlyEarnedBadge(
    val badgeId: Int,
    val name: String,
    val description: String,
    val iconName: String,
    val tier: String
)