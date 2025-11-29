package com.example.locationpins.ui.screen.newfeed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationpins.data.model.Post
import com.example.locationpins.data.model.PostMock
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
class NewsFeedViewModel : ViewModel() {

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

                val posts = fetchPostsFromApi(page = 0, pageSize = _uiState.value.pageSize)

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
                delay(1000)

                val posts = fetchPostsFromApi(page = 0, pageSize = _uiState.value.pageSize)

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
        // Nếu đang load hoặc đã hết dữ liệu thì không làm gì
        if (_uiState.value.isLoadingMore || _uiState.value.hasReachedEnd) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }

            try {
                //Delay để giả lập
                delay(1008)

                val nextPage = _uiState.value.currentPage + 1
                val newPosts = fetchPostsFromApi(
                    page = nextPage,
                    pageSize = _uiState.value.pageSize
                )

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
                    // Toggle react (demo: +1 hoặc -1)
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