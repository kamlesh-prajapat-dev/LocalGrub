package com.example.localgrub.domain.repository

import com.example.localgrub.data.model.NotificationRequest
import com.example.localgrub.data.remote.api.NotificationApi
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Singleton

@Singleton
class NotificationRepository {

    suspend fun sendNotification(notificationRequest: NotificationRequest): Response<ResponseBody> = NotificationApi.api.sendNotification(notificationRequest)
}