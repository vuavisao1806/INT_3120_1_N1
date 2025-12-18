package com.example.locationpins.ui.screen.postDetail

import com.example.locationpins.data.remote.dto.post.PostDto

object PostDetailMockData {
    val mockPostFull = PostDto(
        pinId = 1,
        status = "published",
        title = "Chuyến đi tuyệt vời",
        postId = 1,
        userId = 101,
        userName = "Người dùng Test",
        avatarUrl = "https://example.com/avatar.jpg",
        imageUrl = "https://example.com/post_image.jpg",
        body = "Nội dung bài viết mẫu để test UI. Hôm nay trời rất đẹp!",
        createdAt = "Vừa xong",
        reactionCount = 100,
        commentCount = 50
    )
}
