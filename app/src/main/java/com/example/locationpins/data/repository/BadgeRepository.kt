package com.example.locationpins.data.repository

import com.example.locationpins.data.remote.RetrofitClient
import com.example.locationpins.data.remote.dto.badge.*

class BadgeRepository {
    private val api = RetrofitClient.api

    suspend fun getUserBadges(userId: Int): List<BadgeDto> {
        return api.getUserBadges(GetUserBadgesRequest(userId))
    }

    suspend fun getEarnedBadges(userId: Int, limit: Int = 3): List<BadgeDto> {
        return api.getEarnedBadges(GetEarnedBadgesRequest(userId, limit))
    }

    suspend fun checkAndAwardBadges(userId: Int): CheckBadgesResponse {
        return api.checkAndAwardBadges(CheckAndAwardBadgesRequest(userId))
    }

    suspend fun getBadgeProgress(userId: Int): List<BadgeProgressDto> {
        return api.getBadgeProgress(GetBadgeProgressRequest(userId))
    }
}