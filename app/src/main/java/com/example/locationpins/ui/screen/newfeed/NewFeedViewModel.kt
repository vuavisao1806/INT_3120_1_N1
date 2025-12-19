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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NewsFeedViewModel(
    private val postRepository: PostRepository = PostRepository(),
    private val tagRepository: TagRepository = TagRepository(),
    private val reactionRepository: ReactionRepository = ReactionRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewsFeedUiState())
    val uiState: StateFlow<NewsFeedUiState> = _uiState.asStateFlow()

    init {
        loadInitialPosts()
    }

    /**
     * Load posts lần đầu tiên
     */
    fun loadInitialPosts(tagName: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, filterTag = tagName) }

            val userId = CurrentUser.currentUser!!.userId
            try {
                val (postsWithTags, likedMap) = withContext(Dispatchers.IO) {
                    val postDtos = postRepository.getNewsfeed(
                        userId = userId,
                        limit = _uiState.value.pageSize,
                        offset = 0,
                        tagName = tagName
                    )

                    val posts = postDtos.toPosts()

                    coroutineScope {
                        val results = posts.map { post ->
                            async(Dispatchers.IO) {
                                val tags = tagRepository
                                    .getTagsByPostId(post.postId)
                                    .map { t -> t.name }

                                val liked = runCatching {
                                    reactionRepository.checkReactPost(
                                        postId = post.postId,
                                        userId = userId
                                    )
                                }.onFailure { error ->
                                    Log.e(
                                        "NewsFeedViewModel",
                                        "Error checking reaction for post ${post.postId}: ${error.message}"
                                    )
                                }.getOrDefault(false)

                                Pair(post.copy(tags = tags), liked)
                            }
                        }.awaitAll()

                        val postsWithTags: List<Post> = results.map { it.first }
                        val likedMap = results.associate { it.first.postId to it.second }.toMutableMap()

                        Pair(postsWithTags, likedMap)
                    }
                }

                _uiState.update {
                    it.copy(
                        posts = postsWithTags,
                        isLoading = false,
                        currentPage = 0,
                        hasReachedEnd = postsWithTags.size < it.pageSize,
                        likedPosts = likedMap,
                        filterTag = tagName
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

    fun filterByTag(tagName: String) {
        loadInitialPosts(tagName)
    }

    fun clearTagFilter() {
        loadInitialPosts(null)
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }

            val userId = CurrentUser.currentUser!!.userId
            val currentTag = _uiState.value.filterTag  // Giữ tag hiện tại

            try {
                val (postsWithTags, likedMap) = withContext(Dispatchers.IO) {
                    val postDtos = postRepository.getNewsfeed(
                        userId = userId,
                        limit = _uiState.value.pageSize,
                        offset = 0,
                        tagName = currentTag  // Giữ tag filter
                    )

                    val posts = postDtos.toPosts()

                    coroutineScope {
                        val results = posts.map { post ->
                            async(Dispatchers.IO) {
                                val tags = tagRepository
                                    .getTagsByPostId(post.postId)
                                    .map { t -> t.name }

                                val liked = runCatching {
                                    reactionRepository.checkReactPost(
                                        postId = post.postId,
                                        userId = userId
                                    )
                                }.onFailure { error ->
                                    Log.e(
                                        "NewsFeedViewModel",
                                        "Error checking reaction for post ${post.postId}: ${error.message}"
                                    )
                                }.getOrDefault(false)

                                Pair(post.copy(tags = tags), liked)
                            }
                        }.awaitAll()

                        val postsWithTags: List<Post> = results.map { it.first }
                        val likedMap = results.associate { it.first.postId to it.second }.toMutableMap()

                        Pair(postsWithTags, likedMap)
                    }
                }

                _uiState.update {
                    it.copy(
                        posts = postsWithTags,
                        isRefreshing = false,
                        currentPage = 0,
                        hasReachedEnd = postsWithTags.size < it.pageSize,
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

            val userId = CurrentUser.currentUser!!.userId
            val currentTag = _uiState.value.filterTag

            try {
                val nextPage = _uiState.value.currentPage + 1
                val offset = nextPage * _uiState.value.pageSize

                val (postsWithTags, likedMap) = withContext(Dispatchers.IO) {
                    val postDtos = postRepository.getNewsfeed(
                        userId = userId,
                        limit = _uiState.value.pageSize,
                        offset = offset,
                        tagName = currentTag
                    )

                    val posts = postDtos.toPosts()

                    coroutineScope {
                        val results = posts.map { post ->
                            async(Dispatchers.IO) {
                                val tags = tagRepository
                                    .getTagsByPostId(post.postId)
                                    .map { t -> t.name }

                                val liked = runCatching {
                                    reactionRepository.checkReactPost(
                                        postId = post.postId,
                                        userId = userId
                                    )
                                }.onFailure { error ->
                                    Log.e(
                                        "NewsFeedViewModel",
                                        "Error checking reaction for post ${post.postId}: ${error.message}"
                                    )
                                }.getOrDefault(false)

                                Pair(post.copy(tags = tags), liked)
                            }
                        }.awaitAll()

                        val postsWithTags: List<Post> = results.map { it.first }
                        val likedMap = results.associate { it.first.postId to it.second }.toMutableMap()

                        Pair(postsWithTags, likedMap)
                    }
                }

                _uiState.update {
                    it.copy(
                        posts = it.posts + postsWithTags,
                        isLoadingMore = false,
                        currentPage = nextPage,
                        hasReachedEnd = postsWithTags.size < it.pageSize,
                        likedPosts = likedMap
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
    fun toggleReact(postId: Int) {
        val currentState = _uiState.value

        if (currentState.reactingPostIds.contains(postId)) {
            return
        }

        val isCurrentlyLiked = currentState.likedPosts[postId] ?: false
        val userId = CurrentUser.currentUser!!.userId

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    reactingPostIds = state.reactingPostIds + postId
                )
            }

            try {
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

                _uiState.update { state ->
                    val revertedPosts = state.posts.map { post ->
                        if (post.postId == postId) {
                            val currentCount = post.reactCount as Int
                            val offset = if (isCurrentlyLiked) 1 else -1
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
                _uiState.update { state ->
                    state.copy(
                        reactingPostIds = state.reactingPostIds - postId
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}