package com.example.localgrub.data.remote.api

import com.example.localgrub.data.local.LocalDatabase
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val localDatabase: LocalDatabase
): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        val token = localDatabase.getToken()

        token?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }

        requestBuilder.addHeader("Content-Type", "application/json")
        requestBuilder.addHeader("Accept", "application/json")

        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}