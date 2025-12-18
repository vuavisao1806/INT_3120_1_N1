package com.example.locationpins.ui.screen.newfeed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.locationpins.data.model.Post
import com.example.locationpins.ui.component.PostPreviewForNewsFeed
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsFeedScreen(
    onPostPress: (Post) -> Unit = {},
    onTagPress: (String) -> Unit = {},
    initialTag: String? = null,  // Tag ban đầu (nếu navigate từ màn khác),
    viewModel: NewsFeedViewModel = viewModel()
) {

    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Nếu có initialTag, load ngay lần đầu
    LaunchedEffect(initialTag) {
        if (initialTag != null) {
            viewModel.filterByTag(initialTag)
        }
    }

    // Phát hiện khi scroll đến gần cuối để load more
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItemIndex >= totalItems - 3
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && !uiState.hasReachedEnd) {
            viewModel.loadMorePosts()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            uiState.error != null && uiState.posts.isEmpty() -> {
                ErrorView(
                    message = uiState.error ?: "Có lỗi xảy ra",
                    onRetry = { viewModel.loadInitialPosts(uiState.filterTag) },
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> {
                SwipeRefresh(
                    state = rememberSwipeRefreshState(uiState.isRefreshing),
                    onRefresh = { viewModel.refresh() }
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Tag Filter Header (nếu đang lọc)
                        if (uiState.filterTag != null) {
                            item {
                                TagFilterHeader(
                                    tagName = uiState.filterTag!!,
                                    onClearFilter = { viewModel.clearTagFilter() }
                                )
                            }
                        }

                        // Posts List
                        items(
                            items = uiState.posts,
                            key = { post -> post.postId }
                        ) { post ->
                            val isLiked = uiState.likedPosts[post.postId] == true
                            val isReacting = uiState.reactingPostIds.contains(post.postId)

                            PostPreviewForNewsFeed(
                                post = post,
                                isLiked = isLiked,
                                isReacting = isReacting,
                                modifier = Modifier.padding(horizontal = 12.dp),
                                onReactPress = {
                                    if (!isReacting) {
                                        viewModel.toggleReact(post.postId)
                                    }
                                },
                                onPostPress = { onPostPress(post) },
                                onCommentPress = { onPostPress(post) },
                                onTagPress = { tag ->
                                    // Khi nhấn vào tag, filter theo tag đó
                                    viewModel.filterByTag(tag)
                                }
                            )
                        }

                        // Loading indicator khi load more
                        if (uiState.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }

                        // Thông báo đã hết posts
                        if (uiState.hasReachedEnd && uiState.posts.isNotEmpty()) {
                            item {
                                Text(
                                    text = if (uiState.filterTag != null)
                                        "Bạn đã xem hết tất cả bài viết với tag #${uiState.filterTag}"
                                    else
                                        "Bạn đã xem hết tất cả bài viết, hãy ra ngoài đường và chạm cỏ :V",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    textAlign = TextAlign.Center,
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        // Empty state khi lọc tag nhưng không có bài viết
                        if (uiState.filterTag != null && uiState.posts.isEmpty() && !uiState.isLoading) {
                            item {
                                EmptyTagResults(
                                    tagName = uiState.filterTag!!,
                                    onClearFilter = { viewModel.clearTagFilter() }
                                )
                            }
                        }
                    }
                }

                // Snackbar hiển thị lỗi
                uiState.error?.let { error ->
                    if (uiState.posts.isNotEmpty()) {
                        Snackbar(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp),
                            action = {
                                TextButton(onClick = { viewModel.clearError() }) {
                                    Text("Đóng")
                                }
                            }
                        ) {
                            Text(error)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Header hiển thị tag đang được lọc với nút X để xóa
 */
@Composable
private fun TagFilterHeader(
    tagName: String,
    onClearFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        color = Color(0xFF1976D2).copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Lọc theo tag:",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "#$tagName",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )
            }

            IconButton(
                onClick = onClearFilter,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Xóa bộ lọc",
                    tint = Color(0xFF1976D2)
                )
            }
        }
    }
}

/**
 * Empty state khi không có bài viết nào với tag được chọn
 */
@Composable
private fun EmptyTagResults(
    tagName: String,
    onClearFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "🔍",
            fontSize = 48.sp
        )

        Text(
            text = "Không có bài viết nào",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Không tìm thấy bài viết nào với tag #$tagName trong newfeed của bạn",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Button(
            onClick = onClearFilter,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1976D2)
            )
        ) {
            Text("Xem tất cả bài viết")
        }
    }
}

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )

        Button(onClick = onRetry) {
            Text("Thử lại")
        }
    }
}