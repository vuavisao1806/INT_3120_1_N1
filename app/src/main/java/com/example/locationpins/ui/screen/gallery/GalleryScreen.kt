package com.example.locationpins.ui.screen.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.locationpins.utils.formatCount



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel = viewModel(),
    onPinClick: (Int) -> Unit, // Thêm tham số này để nhận sự kiện từ NavHost
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    // Xóa bỏ currentStep và selectedPinId vì NavHost đã quản lý qua Route

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gallery", fontWeight = FontWeight.SemiBold) },
            )
        }
    ) { paddingValues ->
        Box(modifier = modifier.fillMaxSize().padding(paddingValues)) {
            when {
                uiState.isLoadingPin -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                uiState.error != null -> ErrorView(message = uiState.error!!, onRetry = { viewModel.loadPinsWithPosts() })
                else -> {
                    // Pipeline bắt đầu từ đây: chỉ hiển thị danh sách Pin
                    PinListView(
                        pinSummaries = uiState.pinSummaries,
                        onPinClick = onPinClick // Truyền pinId ngược lại cho NavHost điều hướng
                    )
                }
            }
        }
    }
}

@Composable
private fun PinListView(
    pinSummaries: List<PinSummary>,
    onPinClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Ghim",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${pinSummaries.size} ghim",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        if (pinSummaries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Chưa có ghim nào",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                    Text(
                        "Hãy tạo ghim đầu tiên của bạn!",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(
                    items = pinSummaries,
                    key = { it.pinId }
                ) { pinSummary ->
                    PinGridItem(
                        pinSummary = pinSummary,
                        onClick = { onPinClick(pinSummary.pinId) }
                    )
                }
            }
        }
    }
}

@Composable
fun PinGridItem(
    pinSummary: PinSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick)
    ) {
        // Background image
        AsyncImage(
            model = pinSummary.coverImageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Dark overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )

        // Post count badge
        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .size(48.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.Black.copy(alpha = 0.6f)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = pinSummary.postCount.toString(),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostListView(
    pinId: Int,
    onBackClick: () -> Unit,
    onPostPress: (PostSummary) -> Unit,
    viewModel: GalleryViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // Tự động load bài viết khi vào màn hình
    LaunchedEffect(pinId) {
        viewModel.loadPostsForPin(pinId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ghim", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // Trạng thái đang tải bài viết (Sử dụng isLoading từ ViewModel)
                uiState.isLoadingPost -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Trạng thái lỗi khi tải bài viết
                uiState.error != null -> {
                    ErrorView(
                        message = uiState.error ?: "Đã xảy ra lỗi khi tải bài viết",
                        onRetry = { viewModel.loadPostsForPin(pinId) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Khi tải xong (Dù có bài hay không vẫn vào đây)
                else -> {
                    val pinSummary = uiState.pinSummaries.find { it.pinId == pinId }
                    val posts = uiState.currentPinPosts

                    Column(modifier = Modifier.fillMaxSize()) {
                        // Header (Giữ nguyên UI gốc)
                        if (pinSummary != null) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 16.dp)
                            ) {
                                Text(
                                    text = "Ghim",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${posts.size} bài đăng",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        // Kiểm tra nếu danh sách bài đăng thực sự trống
                        if (posts.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Không có bài đăng nào",
                                    fontSize = 16.sp,
                                    color = Color.Gray
                                )
                            }
                        } else {
                            // Danh sách Grid (Giữ nguyên UI gốc)
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(
                                    items = posts,
                                    key = { it.postId }
                                ) { post ->
                                    PostGridItemWithStats(
                                        post = post,
                                        onClick = { onPostPress(post) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PostGridItemWithStats(
    post: PostSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick)
    ) {
        // Background image
        AsyncImage(
            model = post.imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Stats overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Likes
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Lượt thích",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = formatCount(post.reactionCount),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Comments
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = "Bình luận",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = formatCount(post.commentCount),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
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