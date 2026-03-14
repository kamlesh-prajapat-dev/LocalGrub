package com.example.localgrub.data.remote.api

import com.example.localgrub.BuildConfig
import com.example.localgrub.data.model.api.response.ApiResponse
import com.example.localgrub.data.model.api.response.OtpResponse
import com.example.localgrub.data.model.api.request.OtpRequest
import com.example.localgrub.data.model.api.request.ResendOtpRequest
import com.example.localgrub.data.model.api.request.VerifyOtpRequest
import com.example.localgrub.data.model.api.response.VerifyOtpResponse
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST

private const val BASE_URL = BuildConfig.API_BASE_URL

interface LoginApiService {

    @POST("auth/send-otp")
    suspend fun sendOtp(@Body request: OtpRequest): Response<ApiResponse<OtpResponse>>
    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<ApiResponse<VerifyOtpResponse>>
    @POST("auth/retry-otp")
    suspend fun resendOtp(@Body request: ResendOtpRequest): Response<ApiResponse<OtpResponse>>
}

object LoginApi {
    val api: LoginApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                Json.asConverterFactory("application/json".toMediaType())
            )
            .build()
            .create(LoginApiService::class.java)
    }
}