package com.example.locationpins.ui.screen.profile

import com.example.locationpins.data.model.Badge
import com.example.locationpins.data.model.BadgeProgress
import com.example.locationpins.data.model.User
import com.example.locationpins.data.remote.dto.user.ShowContactRespond
import com.example.locationpins.ui.screen.gallery.PostSummary

sealed interface ProfileMode {
    data object Self : ProfileMode
    data object Friend : ProfileMode
    data object Stranger : ProfileMode
}

data class ProfileUiState(
    val user: User? = null,
    val profileMode: ProfileMode = ProfileMode.Self,
    val showRequestContact: Boolean = false,
    val requestMessage: String = "",
    val isLoading: Boolean = false,
    val showContactRequests: Boolean = false,
    val pendingRequests: List<ShowContactRespond> = emptyList(),

    // Badges hiển thị trên profile (5 cái gần nhất)
    val badges: List<Badge> = emptyList(),

    // Dialog chúc mừng badge mới
    val showNewBadgeDialog: Boolean = false,
    val selectedNewBadge: Badge? = null,

    // Dialog chi tiết badges (có swipe + progress mode)
    val showBadgeDetailDialog: Boolean = false,
    val selectedBadgeIndex: Int = 0,
    val allEarnedBadges: List<Badge> = emptyList(),
    val allBadgeProgress: List<BadgeProgress> = emptyList(),

    val currentPosts: List<PostSummary> = emptyList(),
    val error: String? = null
)