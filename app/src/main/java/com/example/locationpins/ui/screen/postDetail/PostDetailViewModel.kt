package com.example.locationpins.ui.screen.postDetail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationpins.data.repository.CommentRepository
import com.example.locationpins.data.repository.PostRepository
import com.example.locationpins.data.repository.ReactionRepository
import com.example.locationpins.data.repository.SensitiveContentRepository
import com.example.locationpins.data.repository.TagRepository
import com.example.locationpins.ui.screen.login.CurrentUser
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
    private val tagRepository: TagRepository,
    private val sensitiveContentRepository: SensitiveContentRepository,
    private val postId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(PostDetailUiState())
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

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
                val isLiked = reactionRepository.checkReactPost(
                    postId = postId,
                    userId = CurrentUser.currentUser!!.userId
                )

                _uiState.update {
                    it.copy(
                        post = post,
                        comments = comments,
                        tags = tags,
                        isLiked = isLiked,
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
        val currentUserId = CurrentUser.currentUser!!.userId

        viewModelScope.launch {
            try {
                val offset = if (isCurrentlyLiked) -1 else 1
                _uiState.update { state ->
                    state.copy(
                        isLiked = !state.isLiked,
                        post = currentPost.copy(
                            reactionCount = currentPost.reactionCount + offset
                        )
                    )
                }
                if (isCurrentlyLiked) {
                    reactionRepository.cancelReactPost(
                        postId = postId,
                        userId = currentUserId
                    )
                } else {
                    reactionRepository.reactPost(
                        postId = postId,
                        userId = currentUserId
                    )
                }
            } catch (e: Exception) {
                // revert lại trạng thái nếu lỗi
                _uiState.update { it.copy(isLiked = isCurrentlyLiked) }
            }
        }
    }

    fun onSendComment() {
        val currentUserId = CurrentUser.currentUser!!.userId
        Log.d("USER ID WHEN COMMENTING", currentUserId.toString())
        val text = _uiState.value.commentText.trim()
        if (text.isBlank()) return

        viewModelScope.launch {
            try {
                val isSensitive: Boolean = sensitiveContentRepository.isSensitiveText(text = text)
                if (isSensitive) {
                    Log.d("SENSITIVE DETECTION", "The content isn't allowed")
                    // TODO: Thêm hiệu ứng như kiểu cái viền đỏ xung quanh ô nhập hoặc hiện cảnh báo (aler)
                    // TODO: Mr. LDHA sẽ thực hiện
                    // ToDo: nhấn nhiều làn comment bị gửi nhiều lần trùng nhau
                    return@launch
                }
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
                Log.d("COMMENT SUCCESS", "COMMENT")
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
        val currentUserId = CurrentUser.currentUser!!.userId
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
