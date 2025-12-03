package com.example.locationpins.ui.screen.profile

import androidx.lifecycle.ViewModel
import com.example.locationpins.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    /** gọi khi màn hình nhận user + mode */
    fun setUser(user: User, mode: ProfileMode) {
        _uiState.update { current ->
            current.copy(
                user = user,
                profileMode = mode
            )
        }
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
