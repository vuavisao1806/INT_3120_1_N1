package com.example.locationpins.data.model

data class Post (
    val postId: Int,
    val pinId: String,
    val title: String,
    val body: String,
    val imageUrl: String,
    val reactCount: Number,
    val commentCount: Number,
    val tags: List<String> = emptyList()
)