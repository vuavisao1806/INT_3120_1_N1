package com.example.locationpins.ui.screen.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationpins.data.model.User
import com.example.locationpins.data.remote.uriToMultipart
import com.example.locationpins.data.repository.CreatePostRepository
import com.example.locationpins.data.repository.UserRepository
import com.example.locationpins.ui.screen.login.CurrentUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class EditProfileUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val name: String = "",
    val quotes: String = "",
    val location: String = "",
    val email: String = "",
    val website: String = "",
    val currentAvatarUrl: String? = null,
    val selectedImageUri: Uri? = null
)

class EditViewModel(
    private val userRepository: UserRepository = UserRepository(),
    private val createPostRepository: CreatePostRepository = CreatePostRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()


    fun onNameChange(text: String) = _uiState.update { it.copy(name = text) }
    fun onQuotesChange(text: String) = _uiState.update { it.copy(quotes = text) }
    fun onLocationChange(text: String) = _uiState.update { it.copy(location = text) }
    fun onEmailChange(text: String) = _uiState.update { it.copy(email = text) }
    fun onWebsiteChange(text: String) = _uiState.update { it.copy(website = text) }

    fun onImageSelected(uri: Uri?) {
        _uiState.update { it.copy(selectedImageUri = uri) }
    }

    fun initialize(user: User) {
        _uiState.update {
            it.copy(
                name = user.name,
                quotes = user.quote ?: "",
                location = user.location ?: "",
                email = user.userEmail,
                website = user.website ?: "",
                currentAvatarUrl = user.avatarUrl
            )
        }
    }


    fun onSaveClick(context: Context) {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val currentUri = _uiState.value.selectedImageUri
                if (currentUri != null) {
                    val imagePart = uriToMultipart(context, currentUri, "file")
                    val newUrl = createPostRepository.uploadImage(imagePart)
                    _uiState.update { it.copy(currentAvatarUrl = newUrl.url) }
                }
                val result = userRepository.updateProfile(
                    CurrentUser.currentUser!!.userId,
                    name = _uiState.value.name,
                    quotes = _uiState.value.quotes,
                    avatarUrl = _uiState.value.currentAvatarUrl,
                    location = _uiState.value.location,
                    userEmail = _uiState.value.email,
                    website = _uiState.value.website
                )


                if (result.success) {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                    Log.e("EditViewModel", "Update failed")
                }

            } catch (e: Exception) {
                Log.e("EditViewModel", "Error: ${e.message}")
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}