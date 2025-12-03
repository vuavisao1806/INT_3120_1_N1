package com.example.locationpins.ui.screen.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationpins.data.remote.dto.pins.PinDto
import com.example.locationpins.data.remote.dto.post.PostDto
import com.example.locationpins.data.repository.PinRepository
import com.example.locationpins.data.repository.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Data classes để hiển thị
data class PinSummary(
    val pinId: Int,
    val coverImageUrl: String,
    val postCount: Int
)

data class PostSummary(
    val postId: Int,
    val imageUrl: String,
    val reactionCount: Int,
    val commentCount: Int
)

data class GalleryUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val pinSummaries: List<PinSummary> = emptyList(),
    val currentPinPosts: List<PostSummary> = emptyList()
)

class GalleryViewModel(
    private val pinRepository: PinRepository = PinRepository(),
    private val postRepository: PostRepository = PostRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(GalleryUiState())
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    // Mock user ID - trong thực tế lấy từ session/auth
    private val currentUserId = 1

    init {
        loadPinsWithPosts()
    }

    /**
     * Load tất cả pins của user và lấy bài viết đầu tiên làm cover
     */
    fun loadPinsWithPosts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // 1. Lấy tất cả pins của user
                val pins = pinRepository.getPinsByUserId(currentUserId)

                // 2. Với mỗi pin, lấy tất cả posts và đếm
                val pinSummaries = pins.mapNotNull { pin ->
                    try {
                        // Lấy post đầu tiên của pin để làm cover image
                        val post = postRepository.getPost(pin.pinId)

                        PinSummary(
                            pinId = pin.pinId,
                            coverImageUrl = post.imageUrl,
                            postCount = 1 // Tạm thời = 1, sau này query đếm
                        )
                    } catch (e: Exception) {
                        // Pin không có post nào, bỏ qua
                        null
                    }
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        pinSummaries = pinSummaries
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Không thể tải gallery: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Load tất cả posts của 1 pin cụ thể
     */
    fun loadPostsForPin(pinId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // TODO: Tạo API endpoint mới để lấy tất cả posts theo pinId
                // Hiện tại chỉ có 1 post per pin nên lấy luôn
                val post = postRepository.getPost(pinId)

                val postSummary = PostSummary(
                    postId = post.postId,
                    imageUrl = post.imageUrl,
                    reactionCount = post.reactionCount ?: 0,
                    commentCount = post.commentCount
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentPinPosts = listOf(postSummary)
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Không thể tải bài đăng: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}