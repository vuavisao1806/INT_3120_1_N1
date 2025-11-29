package com.example.locationpins.ui.screen.newfeed

import com.example.locationpins.data.model.Post

/**
 * Trạng thái UI của màn hình News Feed
 */
data class NewsFeedUiState(
    // Danh sách posts hiện tại
    val posts: List<Post> = emptyList(),

    // Đang load dữ liệu lần đầu
    val isLoading: Boolean = false,

    // Đang load thêm posts (pagination)
    val isLoadingMore: Boolean = false,

    // Đang refresh
    val isRefreshing: Boolean = false,

    // Có lỗi xảy ra
    val error: String? = null,

    // Đã hết dữ liệu (không còn posts để load)
    val hasReachedEnd: Boolean = false,

    // Trang hiện tại (cho pagination)
    val currentPage: Int = 0,

    // Số lượng posts mỗi trang
    val pageSize: Int = 10
)