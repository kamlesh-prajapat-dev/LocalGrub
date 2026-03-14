package com.example.localgrub.data.remote.api

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import com.example.localgrub.BuildConfig

class WidgetAuthInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        addHeaders(requestBuilder)
        requestBuilder.addHeader("Content-Type", "application/json")
        requestBuilder.addHeader("Accept", "application/json")

        val request = requestBuilder.build()
        return chain.proceed(request)
    }

    private fun addHeaders(requestBuilder: Request.Builder) {
        requestBuilder.addHeader(
            "authkey",
            BuildConfig.MSG91_AUTH_KEY
        )
    }
}