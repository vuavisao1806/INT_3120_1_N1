package com.example.locationpins.ui.screen.postDetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag // Thêm import này
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.locationpins.data.remote.dto.comment.CommentDto
import com.example.locationpins.data.remote.dto.post.PostDto
import com.example.locationpins.data.remote.dto.tag.TagDto
import com.example.locationpins.data.repository.CommentRepository
import com.example.locationpins.data.repository.PostRepository
import com.example.locationpins.data.repository.ReactionRepository
import com.example.locationpins.data.repository.SensitiveContentRepository
import com.example.locationpins.data.repository.TagRepository
import com.example.locationpins.ui.screen.login.CurrentUser
import com.example.locationpins.utils.formatCount

// Object chứa các ID để dùng trong test
object PostDetailTestTags {
    const val LOADING = "PostDetail_Loading"
    const val ERROR_TEXT = "PostDetail_Error"
    const val RETRY_BUTTON = "PostDetail_RetryBtn"
    const val POST_CONTENT = "PostDetail_Content"
    const val LIKE_BUTTON = "PostDetail_LikeBtn"
    const val COMMENT_INPUT = "PostDetail_CommentInput"
    const val SEND_BUTTON = "PostDetail_SendBtn"
    const val COMMENT_ITEM = "PostDetail_CommentItem"
}

@Composable
fun PostDetailScreen(
    postId: String?,
    onNavigateBack: () -> Unit,
    onClickUserName: (Int) -> Unit
) {
    // ViewModel khởi tạo ở đây (Stateful)
    val viewModel = remember {
        PostDetailViewModel(
            postRepository = PostRepository(),
            commentRepository = CommentRepository(),
            reactionRepository = ReactionRepository(),
            tagRepository = TagRepository(),
            sensitiveContentRepository = SensitiveContentRepository(),
            postId = postId?.toIntOrNull() ?: 1
        )
    }

    val uiState by viewModel.uiState.collectAsState()

    // Lấy avatar an toàn để truyền xuống dưới (Tránh crash khi test nếu chưa login)
    val currentUserAvatar = CurrentUser.currentUser?.avatarUrl ?: ""

    // Gọi đến hàm Stateless để hiển thị
    PostDetailContent(
        uiState = uiState,
        currentUserAvatar = currentUserAvatar,
        onClickUserName = onClickUserName,
        onRetry = { viewModel.retry() },
        onLikeClick = { viewModel.onLikeClick() },
        onDeleteComment = { viewModel.onDeleteComment(it) },
        onCommentTextChange = { viewModel.onCommentTextChange(it) },
        onSendComment = { viewModel.onSendComment() }
    )
}

/**
 * Hàm này là Stateless (Không chứa ViewModel).
 * Dùng hàm này để viết UI Test bằng cách truyền Fake Data vào uiState.
 */
@Composable
fun PostDetailContent(
    uiState: PostDetailUiState,
    currentUserAvatar: String,
    onClickUserName: (Int) -> Unit,
    onRetry: () -> Unit,
    onLikeClick: () -> Unit,
    onDeleteComment: (Int) -> Unit,
    onCommentTextChange: (String) -> Unit,
    onSendComment: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .testTag(PostDetailTestTags.LOADING) // Thêm tag
                )
            }

            uiState.error != null && uiState.post == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = uiState.error ?: "Đã xảy ra lỗi",
                        color = Color.Red,
                        fontSize = 16.sp,
                        modifier = Modifier.testTag(PostDetailTestTags.ERROR_TEXT) // Thêm tag
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onRetry,
                        modifier = Modifier.testTag(PostDetailTestTags.RETRY_BUTTON) // Thêm tag
                    ) {
                        Text("Thử lại")
                    }
                }
            }

            uiState.post != null -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF5F5F5))
                        .padding(bottom = 64.dp)
                        .testTag(PostDetailTestTags.POST_CONTENT) // Thêm tag
                ) {
                    // Post Header
                    item {
                        PostHeader(uiState.post!!, onClickUserName = onClickUserName)
                    }

                    // Post Content
                    item {
                        PostContent(uiState.post!!.body)
                    }

                    // Post Image
                    uiState.post!!.imageUrl.let { imageUrl ->
                        item {
                            PostImage(imageUrl)
                        }
                    }

                    // Tags
                    if (uiState.tags.isNotEmpty()) {
                        item {
                            TagsRow(uiState.tags)
                        }
                    }

                    // Like & Comment Count
                    item {
                        InteractionStats(
                            likes = uiState.post!!.reactionCount,
                            comments = uiState.post!!.commentCount,
                            isLiked = uiState.isLiked,
                            onLikeClick = onLikeClick
                        )
                    }

                    // Comments Section Header
                    item {
                        CommentsSectionHeader()
                    }

                    // Comments List
                    items(
                        items = uiState.comments,
                        key = { comment -> comment.commentId }
                    ) { comment ->
                        CommentItem(
                            comment = comment,
                            onDeleteClick = { onDeleteComment(comment.commentId) },
                            onClickUser = onClickUserName
                        )
                    }
                }

                // Bottom Comment Input Box
                BottomCommentInput(
                    commentText = uiState.commentText,
                    userAvatarUrl = currentUserAvatar, // Truyền tham số thay vì gọi trực tiếp singleton
                    onCommentChange = onCommentTextChange,
                    onSendClick = onSendComment,
                    isSubmitting = uiState.isSubmittingComment,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )

                // Show error snackbar if needed
                uiState.error?.let { error ->
                    LaunchedEffect(error) {
                        // You can show a snackbar here if you have SnackbarHost
                    }
                }
            }
        }
    }
}

@Composable
fun PostHeader(post: PostDto, onClickUserName: (Int) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = post.avatarUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable() { onClickUserName(post.userId) },
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {

                Text(
                    text = post.userName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.clickable() { onClickUserName(post.userId) }
                )

                Text(
                    text = post.createdAt,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun PostContent(content: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White
    ) {
        Text(
            text = content,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}

@Composable
fun PostImage(imageUrl: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun TagsRow(tags: List<TagDto>) {
    val tagColor = Color(0xFF1976D2)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tags.forEach { tag ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, tagColor),
                    modifier = Modifier
                ) {
                    Text(
                        text = tag.name,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        color = tagColor
                    )
                }
            }
        }
    }
}

@Composable
fun InteractionStats(
    likes: Int,
    comments: Int,
    isLiked: Boolean,
    onLikeClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White
    ) {
        Column {
            HorizontalDivider(Modifier, DividerDefaults.Thickness, color = Color(0xFFE0E0E0))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (isLiked) "Unlike" else "Like",
                        tint = if (isLiked) Color(0xFFE53935) else Color.Gray,
                        modifier = Modifier
                            .size(22.dp)
                            .clickable { onLikeClick() }
                            .testTag(PostDetailTestTags.LIKE_BUTTON) // Thêm tag cho nút Like
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formatCount(likes),
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                // Comment section
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comment",
                        tint = Color.Gray,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formatCount(comments),
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun CommentsSectionHeader() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White
    ) {
        Column {
            HorizontalDivider(Modifier, DividerDefaults.Thickness, color = Color(0xFFE0E0E0))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bình luận",
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: CommentDto,
    onDeleteClick: () -> Unit,
    onClickUser: (Int) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(PostDetailTestTags.COMMENT_ITEM), // Thêm tag cho comment
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            AsyncImage(
                model = comment.avatarUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable() { onClickUser(comment.userId) },
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF0F0F0)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {

                        Text(
                            text = comment.userName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.clickable() { onClickUser(comment.userId) }
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = comment.content,
                            fontSize = 14.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier.padding(start = 12.dp, top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = comment.createdAt,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun BottomCommentInput(
    commentText: String,
    userAvatarUrl: String, // Nhận URL từ tham số để dễ test
    onCommentChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isSubmitting: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            AsyncImage(
                model = userAvatarUrl, // Dùng biến truyền vào
                contentDescription = null,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Text Input
            TextField(
                value = commentText,
                onValueChange = onCommentChange,
                modifier = Modifier
                    .weight(1f)
                    .testTag(PostDetailTestTags.COMMENT_INPUT), // Tag ô nhập
                placeholder = {
                    Text(
                        text = "Viết bình luận...",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF0F0F0),
                    unfocusedContainerColor = Color(0xFFF0F0F0),
                    disabledContainerColor = Color(0xFFF0F0F0),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(20.dp),
                singleLine = false,
                maxLines = 4,
                enabled = !isSubmitting
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Send Button
            IconButton(
                onClick = {
                    if (commentText.isNotBlank()) {
                        onSendClick()
                    }
                },
                enabled = commentText.isNotBlank() && !isSubmitting,
                modifier = Modifier.testTag(PostDetailTestTags.SEND_BUTTON) // Tag nút gửi
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF1976D2)
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Gửi",
                        tint = if (commentText.isNotBlank()) Color(0xFF1976D2) else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}