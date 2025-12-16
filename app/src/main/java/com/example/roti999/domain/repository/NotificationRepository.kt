package com.example.roti999.domain.repository

import com.example.roti999.data.dto.NotificationRequest
import com.example.roti999.data.remote.api.NotificationApi
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Singleton

@Singleton
class NotificationRepository {

    suspend fun sendNotification(notificationRequest: NotificationRequest): Response<ResponseBody> = NotificationApi.api.sendNotification(notificationRequest)
}