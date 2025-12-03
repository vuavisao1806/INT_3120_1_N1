package com.example.locationpins.ui.screen.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationpins.data.model.User
import com.example.locationpins.data.repository.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    fun enterUsername(value: String) {
        _uiState.value = _uiState.value.copy(username = value)
    }

    fun enterPassword(value: String) {
        _uiState.update { curent ->
            curent.copy(password = value)
        }
    }

    fun togglePasswordVisibility() {
        _uiState.update { curent ->
            curent.copy(
                isPasswordVisible = !_uiState.value.isPasswordVisible
            )
        }
    }

    fun login() {
        viewModelScope.launch {
            _uiState.update { curent -> curent.copy(isLoading = true, errorMessage = null) }

//            delay(1200)

            val userName = _uiState.value.username
            val password = _uiState.value.password

            val response = userRepository.login(userName, password)
            if (!response.success) {
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = "Sai tên đăng nhập hoặc mật khẩu!"
                    )
                }
            } else {
                CurrentUser.currentUser = User(
                    userId = 1,
                    userName = "linhnguyen",
                    location = "Hồ Chí Minh, Việt Nam",
                    avatarUrl = "https://example.com/avatar/linh.png",
                    quote = "Sống là trải nghiệm.",
                    name = "Nguyễn Thị Linh",
                    quantityPin = 34,
                    quantityReact = 1280,
                    quantityComment = 256,
                    userEmail = "linh.nguyen@example.com",
                    phoneNum = "+84 912 345 678",
                    website = "https://linhnguyen.dev",
                    quantityContact = 5
                )
//                CurrentUser.currentUser = User(
//                    userId = response.user.useId,
//                    userName = response.user.useName,
//                    location = response.user.location,
//                    avatarUrl = TODO(),
//                    quote = response.user.quotes,
//                    name = response.user.name,
//                    quantityPin = TODO(),
//                    quantityReact = TODO(),
//                    quantityComment = TODO(),
//                    quantityContact = TODO(),
//                    userEmail = response.user.useEmail,
//                    phoneNum = response.user.phoneNum,
//                    website = response.user.website,
//                )
            }
        }
    }
}

object CurrentUser {
    var currentUser: User? = null
}

