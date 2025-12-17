package com.example.locationpins.ui.screen.profile


import com.example.locationpins.data.model.User
import com.example.locationpins.data.remote.dto.user.ShowContactRespond
import com.example.locationpins.ui.screen.gallery.PostSummary

sealed interface ProfileMode {
    data object Self : ProfileMode
    data object Friend : ProfileMode
    data object Stranger  : ProfileMode
}
data class ProfileUiState(
    val user: User? = null,
    val profileMode: ProfileMode = ProfileMode.Self,
    val showRequestContact: Boolean = false,
    val requestMessage: String = "",
    val isLoading: Boolean=false,
    val showContactRequests: Boolean = false, // Trạng thái hiển thị BottomSheet
    val pendingRequests: List<ShowContactRespond> = emptyList(),
    val currentPosts:List<PostSummary> = emptyList(),
    val error: String?=null
)
