package com.example.locationpins.data.remote


import com.example.locationpins.data.remote.dto.pins.PinDto
import com.example.locationpins.data.remote.dto.pins.GetPinListByUserIdRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    // Lấy danh sách pins theo user_id
    @POST("/pins/get/user-id")
    suspend fun getPinsByUserId(
        @Body body: GetPinListByUserIdRequest
    ): List<PinDto>

}
