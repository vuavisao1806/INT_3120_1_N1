package com.example.locationpins.ui.screen.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationpins.data.remote.dto.post.PostDto
import com.example.locationpins.data.repository.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SelectedPinUiState(
    val posts: List<PostDto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class SelectedPinViewModel(
    private val postRepo: PostRepository = PostRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(SelectedPinUiState())
    val uiState: StateFlow<SelectedPinUiState> = _uiState.asStateFlow()

    fun loadPostsByPinId(pinId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val result = postRepo.getPostByPinIdRequestFromMapScreen(pinId)

                _uiState.update {
                    it.copy(
                        posts = result,
                        isLoading = false,
                        errorMessage = if (result.isEmpty()) "Không tìm thấy bài viết" else null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Đã xảy ra lỗi: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

}