package com.example.locationpins.ui.screen.newfeed

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationpins.data.mapper.toPosts
import com.example.locationpins.data.model.Post
import com.example.locationpins.data.repository.PostRepository
import com.example.locationpins.data.repository.ReactionRepository
import com.example.locationpins.data.repository.TagRepository
import com.example.locationpins.ui.screen.login.CurrentUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel cho màn hình News Feed
 */
class NewsFeedViewModel(
    private val postRepository: PostRepository = PostRepository(),
    private val tagRepository: TagRepository = TagRepository(),
    private val reactionRepository: ReactionRepository = ReactionRepository()
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

            val userId = CurrentUser.currentUser!!.userId
            try {
//                val result = withContext(Dispatchers.IO) {
                val postDtos = postRepository.getNewsfeed(
                    userId = userId,
                    limit = _uiState.value.pageSize,
                    offset = 0
                )

                val posts = postDtos.toPosts()

                val postsWithTags: List<Post> = posts.map { post ->
                    val tags = tagRepository
                        .getTagsByPostId(post.postId)
                        .map { t -> t.name }

                    post.copy(tags = tags)
                }

                // Load trạng thái like cho tất cả posts
                val likedMap = mutableMapOf<Int, Boolean>()
                postsWithTags.forEach { post ->
                    try {
                        val isLiked = reactionRepository.checkReactPost(
                            postId = post.postId,
                            userId = userId
                        )
                        likedMap[post.postId] = isLiked
                    } catch (e: Exception) {
                        Log.e("NewsFeedViewModel", "Error checking reaction for post ${post.postId}: ${e.message}")
                        likedMap[post.postId] = false
                    }
                }
//                    Pair(postsWithTags, likedMap)
//                }
//                val (postsWithTags, likedMap) = result

                _uiState.update {
                    it.copy(
                        posts = postsWithTags,
                        isLoading = false,
                        currentPage = 0,
                        hasReachedEnd = postsWithTags.size < it.pageSize,
                        likedPosts = likedMap
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
                    userId = CurrentUser.currentUser!!.userId,
                    limit = _uiState.value.pageSize,
                    offset = 0
                )

                val posts = postDtos.toPosts()

                val postsWithTags: List<Post> = posts.map { post ->
                    val tags = tagRepository
                        .getTagsByPostId(post.postId)
                        .map { t -> t.name }

                    post.copy(tags = tags)
                }

                // Load trạng thái like cho tất cả posts
                val likedMap = mutableMapOf<Int, Boolean>()
                postsWithTags.forEach { post ->
                    try {
                        val isLiked = reactionRepository.checkReactPost(
                            postId = post.postId,
                            userId = CurrentUser.currentUser!!.userId
                        )
                        likedMap[post.postId] = isLiked
                    } catch (e: Exception) {
                        Log.e("NewsFeedViewModel", "Error checking reaction for post ${post.postId}: ${e.message}")
                        likedMap[post.postId] = false
                    }
                }

                _uiState.update {
                    it.copy(
                        posts = postsWithTags,
                        isRefreshing = false,
                        currentPage = 0,
                        hasReachedEnd = posts.size < it.pageSize,
                        likedPosts = likedMap
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

                val newPostDtos = postRepository.getNewsfeed(
                    userId = CurrentUser.currentUser!!.userId,
                    limit = _uiState.value.pageSize,
                    offset = offset
                )

                val newPosts = newPostDtos.toPosts()

                // Load trạng thái like cho posts mới
                val newLikedMap = _uiState.value.likedPosts.toMutableMap()
                newPosts.forEach { post ->
                    try {
                        val isLiked = reactionRepository.checkReactPost(
                            postId = post.postId,
                            userId = CurrentUser.currentUser!!.userId
                        )
                        newLikedMap[post.postId] = isLiked
                    } catch (e: Exception) {
                        Log.e("NewsFeedViewModel", "Error checking reaction for post ${post.postId}: ${e.message}")
                        newLikedMap[post.postId] = false
                    }
                }

                _uiState.update {
                    it.copy(
                        posts = it.posts + newPosts,
                        isLoadingMore = false,
                        currentPage = nextPage,
                        hasReachedEnd = newPosts.size < it.pageSize,
                        likedPosts = newLikedMap
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

    suspend fun checkPostReact(postId: Int): Boolean {
        return reactionRepository.checkReactPost(
            postId = postId,
            userId = CurrentUser.currentUser!!.userId
        )
    }

    /**
     * React/Unreact một post
     */
    fun toggleReact(postId: Int) {
        val currentState = _uiState.value

        // Kiểm tra nếu post đang được xử lý thì không làm gì
        if (currentState.reactingPostIds.contains(postId)) {
            return
        }

        val isCurrentlyLiked = currentState.likedPosts[postId] ?: false
        val userId = CurrentUser.currentUser!!.userId
        viewModelScope.launch {
            // Thêm postId vào set đang xử lý
            _uiState.update { state ->
                state.copy(
                    reactingPostIds = state.reactingPostIds + postId
                )
            }

            try {
                // Optimistic update: Cập nhật UI trước
                _uiState.update { state ->
                    val updatedPosts = state.posts.map { post ->
                        if (post.postId == postId) {
                            val currentCount = post.reactCount as Int
                            val offset = if (isCurrentlyLiked) -1 else 1
                            post.copy(reactCount = currentCount + offset)
                        } else {
                            post
                        }
                    }

                    val updatedLikedMap = state.likedPosts.toMutableMap()
                    updatedLikedMap[postId] = !isCurrentlyLiked

                    state.copy(
                        posts = updatedPosts,
                        likedPosts = updatedLikedMap
                    )
                }

                // Gọi API
                if (isCurrentlyLiked) {
                    reactionRepository.cancelReactPost(
                        postId = postId,
                        userId = userId
                    )
                } else {
                    reactionRepository.reactPost(
                        postId = postId,
                        userId = userId
                    )
                }

            } catch (e: Exception) {
                Log.e("NewsFeedViewModel", "Error toggling reaction: ${e.message}")

                // Revert lại nếu có lỗi
                _uiState.update { state ->
                    val revertedPosts = state.posts.map { post ->
                        if (post.postId == postId) {
                            val currentCount = post.reactCount as Int
                            val offset = if (isCurrentlyLiked) 1 else -1 // Đảo ngược offset
                            post.copy(reactCount = currentCount + offset)
                        } else {
                            post
                        }
                    }

                    val revertedLikedMap = state.likedPosts.toMutableMap()
                    revertedLikedMap[postId] = isCurrentlyLiked

                    state.copy(
                        posts = revertedPosts,
                        likedPosts = revertedLikedMap,
                        error = "Không thể ${if (isCurrentlyLiked) "hủy thích" else "thích"}: ${e.message}"
                    )
                }
            } finally {
                // Xóa postId khỏi set đang xử lý
                _uiState.update { state ->
                    state.copy(
                        reactingPostIds = state.reactingPostIds - postId
                    )
                }
            }
        }
    }

    /**
     * Xóa thông báo lỗi
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}