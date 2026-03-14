package com.example.localgrub.di

import com.example.localgrub.BuildConfig
import com.example.localgrub.data.local.LocalDatabase
import com.example.localgrub.data.remote.api.AuthInterceptor
import com.example.localgrub.data.remote.api.LoginApi
import com.example.localgrub.data.remote.api.LoginApiService
import com.example.localgrub.data.remote.api.NotificationApiService
import com.example.localgrub.data.remote.api.OtpApi
import com.example.localgrub.data.remote.api.OtpApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val BASE_URL = BuildConfig.API_BASE_URL

    @Provides
    @Singleton
    fun provideAuthInterceptor(localDatabase: LocalDatabase): AuthInterceptor {
        return AuthInterceptor(localDatabase)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideLoginApiService(): LoginApiService {
        return LoginApi.api
    }

    @Provides
    @Singleton
    fun provideNotificationApiService(retrofit: Retrofit): NotificationApiService {
        return retrofit.create(NotificationApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideOtpApiService(): OtpApiService {
        return OtpApi.api
    }
}