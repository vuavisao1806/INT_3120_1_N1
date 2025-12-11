package com.example.locationpins.data.mapper

import com.example.locationpins.data.model.Post
import com.example.locationpins.data.remote.dto.post.PostDto

/**
 * Convert PostDto từ API sang Post model để dùng trong UI
 */
fun PostDto.toPost(): Post {
    return Post(
        postId = this.postId,
        pinId = this.pinId.toString(),
        title = this.title,
        body = this.body,
        imageUrl = this.imageUrl,
        reactCount = this.reactionCount,
        commentCount = this.commentCount,
        tags = emptyList()  // Tags sẽ fetch riêng nếu cần
    )
}

fun List<PostDto>.toPosts(): List<Post> {
    return this.map { it.toPost() }
}