package com.example.locationpins.ui.screen.profile


import com.example.locationpins.data.model.User

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
    val isLoading: Boolean=false
)
