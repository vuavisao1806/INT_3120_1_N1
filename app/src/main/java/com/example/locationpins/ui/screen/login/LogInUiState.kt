package com.example.locationpins.ui.screen.login


data class LoginUiState(

    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val email: String = "",
    val isLoading: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val errorMessage: String? = null
)
