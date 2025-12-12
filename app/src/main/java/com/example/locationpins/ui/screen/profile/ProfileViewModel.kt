package com.example.locationpins.ui.screen.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationpins.data.model.User
import com.example.locationpins.data.remote.dto.user.toUser
import com.example.locationpins.data.repository.UserRepository
import com.example.locationpins.ui.screen.login.CurrentUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(private val userRepository: UserRepository = UserRepository()) :
    ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    /** gọi khi màn hình nhận user + mode */
    fun setUser(userId: Int) {
        Log.e("ProfileViewModel", "Invalid User ID: $userId. Skipping API call.")
        _uiState.update { current ->
            current.copy(
                isLoading = true
            )
        }
        viewModelScope.launch {

            try {
                val userDto = userRepository.getUser(userId)

                val currentUserId = CurrentUser.currentUser?.userId
                Log.e("ProfileViewModel", "Current user id: $currentUserId")
                if (userDto != null) {
                    val user = userDto.toUser()
                    val mode = if (currentUserId != null) {
                        when {
                            userDto.userId == currentUserId -> ProfileMode.Self
                            userRepository.isFriend(
                                userDto.userId,
                                currentUserId
                            ) -> ProfileMode.Friend

                            else -> ProfileMode.Stranger
                        }
                    } else {
                        ProfileMode.Stranger
                    }
                    _uiState.update {
                        it.copy(
                            user = user,
                            profileMode = mode,
                            isLoading = false
                        )
                    }

                } else {
                    // Trường hợp API trả về 200 OK nhưng không có user (userDto null)
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error: ${e.message}")
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }


    fun setUser(user: User, profileMode: ProfileMode) {
        _uiState.update { current ->
            current.copy(
                user = user,
                profileMode = profileMode
            )
        }
    }

    fun getUser(): User? {
        return _uiState.value.user
    }

    /** bấm nút Get contact (chỉ meaningful với Stranger) */
    fun onGetContactClick() {
        _uiState.update { current ->
            if (current.profileMode is ProfileMode.Stranger) {
                current.copy(
                    showRequestContact = true,
                    requestMessage = ""
                )
            } else {
                current
            }
        }
    }

    fun onDismissRequestDialog() {
        _uiState.update { current ->
            current.copy(
                showRequestContact = false,
            )
        }
    }

    fun onMessageChange(newText: String) {
        if (newText.length <= 500) {
            _uiState.update { current ->
                current.copy(requestMessage = newText)
            }
        }
    }

    fun getMessage(): String {
        return _uiState.value.requestMessage
    }

    fun onSendClick() {
        val msg = _uiState.value.requestMessage.trim()
        if (msg.isBlank()) return
        _uiState.update { current ->
            current.copy(
                showRequestContact = false,
            )
        }
    }


}
