package com.example.locationpins.data.mapper

import com.example.locationpins.data.model.Badge
import com.example.locationpins.data.model.BadgeProgress
import com.example.locationpins.data.model.NewlyEarnedBadge
import com.example.locationpins.data.remote.dto.badge.BadgeDto
import com.example.locationpins.data.remote.dto.badge.BadgeProgressDto
import com.example.locationpins.data.remote.dto.badge.NewlyEarnedBadgeDto

fun BadgeDto.toBadge(): Badge = Badge(
    badgeId = badgeId,
    name = name,
    description = description,
    iconName = iconName,
    tier = tier,
    earnedAt = earnedAt,
    isEarned = isEarned
)

fun List<BadgeDto>.toBadges(): List<Badge> = map { it.toBadge() }

fun BadgeProgressDto.toBadgeProgress(): BadgeProgress = BadgeProgress(
    badgeId = badgeId,
    name = name,
    description = description,
    iconName = iconName,
    tier = tier,
    requirementType = requirementType,
    requirementValue = requirementValue,
    currentValue = currentValue,
    progressPercentage = progressPercentage,
    isEarned = isEarned
)

fun List<BadgeProgressDto>.toBadgeProgressList(): List<BadgeProgress> = map { it.toBadgeProgress() }

fun NewlyEarnedBadgeDto.toNewlyEarnedBadge(): NewlyEarnedBadge = NewlyEarnedBadge(
    badgeId = badgeId,
    name = name,
    description = description,
    iconName = iconName,
    tier = tier
)

fun List<NewlyEarnedBadgeDto>.toNewlyEarnedBadges(): List<NewlyEarnedBadge> =
    map { it.toNewlyEarnedBadge() }