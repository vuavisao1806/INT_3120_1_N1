package com.example.locationpins.ui.screen.postDetail
import com.example.locationpins.data.remote.dto.comment.CommentDto
import com.example.locationpins.data.remote.dto.post.PostDto
import com.example.locationpins.data.remote.dto.tag.TagDto

data class PostDetailUiState(
    val post: PostDto? = null,
    val comments: List<CommentDto> = emptyList(),
    val tags: List<TagDto> = emptyList(),
    val isLiked: Boolean = false,
    val onParentComment: CommentDto? = null,
    val commentText: String = "",
    val isSubmittingComment: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)
