package com.example.locationpins.data.repository

import com.example.locationpins.data.remote.RetrofitClient
import com.example.locationpins.data.remote.dto.user.LoginRequest
import com.example.locationpins.data.remote.dto.user.LoginResponse
import com.example.locationpins.data.remote.dto.user.RegisterRequest
import com.example.locationpins.data.remote.dto.user.RegisterResponse

class UserRepository {
    private val api = RetrofitClient.api

    suspend fun login(
        userName: String,
        userPassword: String
    ): LoginResponse {
        return api.login(LoginRequest(userName, userPassword))
    }

    suspend fun register(
        userName: String,
        userPassword: String,
        userEmail: String
    ): RegisterResponse {
        return api.register(RegisterRequest(userName, userEmail, userPassword))
    }
}