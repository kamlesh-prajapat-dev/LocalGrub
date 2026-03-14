package com.example.localgrub.domain.repository

import com.example.localgrub.data.model.api.request.NotificationRequest
import com.example.localgrub.domain.model.result.NotificationResult

interface NotificationRepository {
    suspend fun sendNotification(notificationRequest: NotificationRequest): NotificationResult

    suspend fun saveToken(userId: String, token: String): NotificationResult

    suspend fun updateToken(userId: String, token: String): NotificationResult

    suspend fun deleteToken(userId: String): NotificationResult
}