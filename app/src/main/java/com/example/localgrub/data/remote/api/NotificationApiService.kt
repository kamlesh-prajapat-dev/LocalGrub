package com.example.localgrub.data.remote.api

import com.example.localgrub.data.model.api.response.ApiResponse
import com.example.localgrub.data.model.api.request.NotificationRequest
import com.example.localgrub.data.model.api.request.TokenRequest
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface NotificationApiService {
    @POST("notifications/send-notification-to-owner")
    suspend fun sendNotification(@Body notificationRequest: NotificationRequest): Response<ApiResponse<String>>

    @POST("notifications/id/{id}")
    suspend fun saveToken(@Path("id") id: String, @Body request: TokenRequest): Response<ApiResponse<String>>

    @PUT("notifications/id/{id}")
    suspend fun updateToken(@Path("id") id: String, @Body token: String): Response<ApiResponse<String>>

    @DELETE("notifications/id/{id}")
    suspend fun deleteToken(@Path("id") id: String): Response<ApiResponse<String>>
}