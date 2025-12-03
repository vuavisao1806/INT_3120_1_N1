package com.example.locationpins.data.model

data class User(
    val userId: Int,
    val userName: String,
    val location: String,
    val avatarUrl: String,
    val quote: String,
    val name: String,
    val quantityPin: Int,
    val quantityReact: Int,
    val quantityComment: Int,
    val quantityContact: Int,
    val userEmail: String,
    val phoneNum: String,
    val website: String,
)
