package com.example.localgrub.data.remote.api

import com.example.localgrub.BuildConfig
import com.example.localgrub.data.model.api.request.WidgetOtpRequest
import com.example.localgrub.data.model.api.response.OtpResponse
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST

private const val BASE_URL = BuildConfig.MSG91_API_BASE_URL

interface OtpApiService {

    @POST("sendOtp")
    suspend fun sendOtp(@Body request: WidgetOtpRequest): Response<OtpResponse>
}

object OtpApi {
    val client = OkHttpClient.Builder()
        .addInterceptor(WidgetAuthInterceptor())
        .build()

    val api: OtpApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(
                Json.asConverterFactory("application/json".toMediaType())
            )
            .build()
            .create(OtpApiService::class.java)
    }
}