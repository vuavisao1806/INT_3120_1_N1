package com.example.locationpins.ui.screen.gallery

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationpins.data.repository.PinRepository
import com.example.locationpins.data.repository.PostRepository
import com.example.locationpins.data.repository.TagRepository
import com.example.locationpins.ui.screen.login.CurrentUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Data classes để hiển thị
data class PinSummary(
    val pinId: Int,
    val coverImageUrl: String,
    val postCount: Long
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
    private val postRepository: PostRepository = PostRepository(),
    private val tagRepository: TagRepository = TagRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(GalleryUiState())
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    // Mock user ID - trong thực tế lấy từ session/auth


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
                // TODO: pin.quantity không khớp với số lượng bài viết khi bấm vào
                // Lý do là vì query đang count cnt (số lượng post tại pin đó)
                // Còn khi bấm vào thì nó là số lượng bài mình tự đăng + số bài theo tag mình yêu thích nhất
                val pins = postRepository.getPreviewPins(CurrentUser.currentUser!!.userId)

                // 2. Với mỗi pin, lấy tất cả posts và đếm
                val pinSummaries = pins.mapNotNull { pin ->
                    try {
                        // Lấy min image của pin để làm cover image
                        PinSummary(
                            pinId = pin.pinId,
                            coverImageUrl = pin.pinImage,
                            postCount = pin.quantity
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
                val posts = postRepository.getPostByPin(pinId)
                Log.d("BEFORE: ", posts.size.toString())

                if (CurrentUser.favoriteTags == null) {
                    CurrentUser.favoriteTags = tagRepository.getFavoriteTagsByUserId(
                        userId = CurrentUser.currentUser!!.userId,
                        numberTags = 3
                    )
                }
                val favoriteTags = CurrentUser.favoriteTags.orEmpty()

                val favoriteTagsName: Set<String> = favoriteTags.map { it.name }.toSet()

                val favoritePosts = posts.filter { post ->
                    val tags: List<String> = tagRepository
                        .getTagsByPostId(post.postId)
                        .map { it.name }
                    tags.any { it in favoriteTagsName } || post.userId == CurrentUser.currentUser!!.userId
                }
                Log.d("BEFORE: ", favoritePosts.size.toString())

                val postSummaries = favoritePosts.map { post ->
                    PostSummary(
                        postId = post.postId,
                        imageUrl = post.imageUrl,
                        reactionCount = post.reactionCount,
                        commentCount = post.commentCount
                    )
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentPinPosts = postSummaries
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