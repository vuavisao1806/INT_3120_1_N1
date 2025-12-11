package com.example.locationpins.ui.screen.newfeed

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
) {
    // Khởi tạo ViewModel bên trong composable
    val viewModel: NewsFeedViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Phát hiện khi scroll đến gần cuối để load more
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

            // Load more khi còn 3 items nữa là đến cuối
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
            // Loading lần đầu
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Có lỗi và không có posts
            uiState.error != null && uiState.posts.isEmpty() -> {
                ErrorView(
                    message = uiState.error ?: "Có lỗi xảy ra",
                    onRetry = { viewModel.loadInitialPosts() },
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Hiển thị danh sách posts
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
                        items(
                            items = uiState.posts,
                            key = { post -> post.postId }
                        ) { post ->
                            PostPreviewForNewsFeed(
                                post = post,
                                modifier = Modifier.padding(horizontal = 12.dp),
//                                onReactPress = viewModel::toggleReact,
                                onPostPress = { onPostPress(post) },
                                onCommentPress = { onPostPress(post) },
                                onTagPress = onTagPress
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
                                    text = "Bạn đã xem hết tất cả bài viết, hãy ra ngoài đường và chạm cỏ :V",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    textAlign = TextAlign.Center,
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodyMedium
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