package com.example.locationpins.ui.screen.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationpins.data.model.User
import com.example.locationpins.data.remote.dto.user.toUser
import com.example.locationpins.data.repository.UserRepository
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

    fun login(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _uiState.update { curent -> curent.copy(isLoading = true, errorMessage = null) }


            val userName = _uiState.value.username
            val password = _uiState.value.password
            try {
                Log.d("LOGIN_DEBUG", "Đang gọi API tới Server...")
                val response = userRepository.login(userName, password)
                if (!response.success) {
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            errorMessage = "Sai tên đăng nhập hoặc mật khẩu!"
                        )
                    }
                    onResult(false)
                } else {
                    CurrentUser.currentUser = response.user!!.toUser()
                    onResult(true)
                }

            } catch (e: Exception) {
                Log.e("Login", "The error occurs when login: ${e.message}")
                e.printStackTrace()

                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Lỗi hệ thống: ${e.message}")
                }
                onResult(false)
            }

        }
    }
}


object CurrentUser {
    var currentUser: User? = null
}

