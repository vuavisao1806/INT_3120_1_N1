package com.example.locationpins.ui.screen.newfeed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationpins.data.mapper.toPosts
import com.example.locationpins.data.model.Post
import com.example.locationpins.data.model.PostMock
import com.example.locationpins.data.repository.PostRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel cho màn hình News Feed
 *
 */
class NewsFeedViewModel(
    private val postRepository: PostRepository = PostRepository(),
    private val userId: Int = 1
) : ViewModel() {

    // uiState private
    private val _uiState = MutableStateFlow(NewsFeedUiState())
    val uiState: StateFlow<NewsFeedUiState> = _uiState.asStateFlow()

    init {
        // Load dữ liệu ban đầu
        loadInitialPosts()
    }

    /**
     * Load posts lần đầu tiên
     */
    fun loadInitialPosts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val postDtos = postRepository.getNewsfeed(
                    userId = userId,
                    limit = _uiState.value.pageSize,
                    offset = 0
                )

                val posts = postDtos.toPosts()

                _uiState.update {
                    it.copy(
                        posts = posts,
                        isLoading = false,
                        currentPage = 0,
                        hasReachedEnd = posts.size < it.pageSize
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Không thể tải dữ liệu: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Refresh toàn bộ feed
     */

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }

            try {

                val postDtos = postRepository.getNewsfeed(
                    userId = userId,
                    limit = _uiState.value.pageSize,
                    offset = 0
                )

                val posts = postDtos.toPosts()

                _uiState.update {
                    it.copy(
                        posts = posts,
                        isRefreshing = false,
                        currentPage = 0,
                        hasReachedEnd = posts.size < it.pageSize
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        error = "Không thể làm mới: ${e.message}"
                    )
                }
            }
        }
    }


    /**
     * Load thêm posts khi scroll đến cuối
     */
    fun loadMorePosts() {
        if (_uiState.value.isLoadingMore || _uiState.value.hasReachedEnd) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }

            try {
                val nextPage = _uiState.value.currentPage + 1
                val offset = nextPage * _uiState.value.pageSize

                // ⭐ GỌI API THẬT
                val newPostDtos = postRepository.getNewsfeed(
                    userId = userId,
                    limit = _uiState.value.pageSize,
                    offset = offset
                )

                val newPosts = newPostDtos.toPosts()

                _uiState.update {
                    it.copy(
                        posts = it.posts + newPosts,
                        isLoadingMore = false,
                        currentPage = nextPage,
                        hasReachedEnd = newPosts.size < it.pageSize
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingMore = false,
                        error = "Không thể tải thêm: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * React/Unreact một post
     */
    fun toggleReact(postId: String) {
        _uiState.update { currentState ->
            val updatedPosts = currentState.posts.map { post ->
                if (post.postId == postId) {
                    val currentCount = post.reactCount as Int
                    post.copy(reactCount = currentCount + 1)
                } else {
                    post
                }
            }
            currentState.copy(posts = updatedPosts)
        }
    }

    /**
     * Xóa thông báo lỗi
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Simulate API call để fetch posts
     * Sẽ thay thế bằng Api thật
     */
    private suspend fun fetchPostsFromApi(page: Int, pageSize: Int): List<Post> {
        // Giả lập phân trang với dữ liệu mock
        val allPosts = PostMock.samplePosts
        val startIndex = page * pageSize
        val endIndex = minOf(startIndex + pageSize, allPosts.size)

        return if (startIndex < allPosts.size) {
            allPosts.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
    }
}