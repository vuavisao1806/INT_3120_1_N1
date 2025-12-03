package com.example.locationpins.ui.screen.postDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationpins.data.repository.CommentRepository
import com.example.locationpins.data.repository.PostRepository
import com.example.locationpins.data.repository.ReactionRepository
import com.example.locationpins.data.repository.TagRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.max

class PostDetailViewModel(
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val reactionRepository: ReactionRepository,
    private val tagRepository: TagRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PostDetailUiState())
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    // tạm thời hard-code, sau này truyền qua nav args cho đẹp
    private val currentUserId = 2
    private val postId = 2

    init {
        loadPostDetails()
    }

    private fun loadPostDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val post = postRepository.getPost(postId)
                val comments = commentRepository.getPostComments(postId)
                val tags = tagRepository.getTagsByPostId(postId)

                _uiState.update {
                    it.copy(
                        post = post,
                        comments = comments,
                        tags = tags,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Đã xảy ra lỗi khi tải bài viết"
                    )
                }
            }
        }
    }

    fun onCommentTextChange(text: String) {
        _uiState.update { it.copy(commentText = text) }
    }

    fun onLikeClick() {
        val currentState = _uiState.value
        val isCurrentlyLiked = currentState.isLiked
        val currentPost = currentState.post ?: return

        viewModelScope.launch {
            try {
                if (isCurrentlyLiked) {
                    // Unlike
                    reactionRepository.cancelReactPost(
                        postId = postId,
                        userId = currentUserId
                    )
                    _uiState.update { state ->
                        val post = state.post ?: currentPost
                        state.copy(
                            isLiked = false,
                            post = post.copy(
                                reactionCount = (post.reactionCount ?: 1) - 1
                            )
                        )
                    }
                } else {
                    // Like
                    reactionRepository.reactPost(
                        postId = postId,
                        userId = currentUserId
                    )
                    _uiState.update { state ->
                        val post = state.post ?: currentPost
                        state.copy(
                            isLiked = true,
                            post = post.copy(
                                reactionCount = (post.reactionCount ?: 0) + 1
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                // revert lại trạng thái nếu lỗi
                _uiState.update { it.copy(isLiked = isCurrentlyLiked) }
            }
        }
    }

    fun onSendComment() {
        val text = _uiState.value.commentText.trim()
        if (text.isBlank()) return

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSubmittingComment = true, error = null) }

                commentRepository.createComment(
                    postId = postId,
                    userId = currentUserId,
                    content = text
                )

                val updatedComments = commentRepository.getPostComments(postId)
                val currentPost = _uiState.value.post ?: return@launch

                _uiState.update {
                    it.copy(
                        commentText = "",
                        comments = updatedComments,
                        isSubmittingComment = false,
                        post = currentPost.copy(
                            commentCount = currentPost.commentCount + 1
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmittingComment = false,
                        error = e.message ?: "Không thể gửi bình luận"
                    )
                }
            }
        }
    }

    fun onDeleteComment(commentId: Int) {
        viewModelScope.launch {
            try {
                commentRepository.cancelComment(
                    commentId = commentId,
                    userId = currentUserId
                )

                val updatedComments = commentRepository.getPostComments(postId)
                val currentPost = _uiState.value.post ?: return@launch

                _uiState.update {
                    it.copy(
                        comments = updatedComments,
                        post = currentPost.copy(
                            commentCount = max(0, currentPost.commentCount - 1)
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Không thể xoá bình luận"
                    )
                }
            }
        }
    }

    fun retry() {
        loadPostDetails()
    }
}
