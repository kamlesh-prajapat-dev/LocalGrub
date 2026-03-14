package com.example.localgrub.domain.usecase

import com.example.localgrub.domain.model.result.NotificationResult
import com.example.localgrub.domain.repository.NotificationRepository
import com.example.localgrub.util.AppLogger
import com.example.localgrub.util.TokenManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
){
    suspend fun saveToken(userId: String) {
        val token = TokenManager.getFCMToken()
        if (token != null) {
            when(val result = notificationRepository.saveToken(userId = userId, token = token)) {
                is NotificationResult.Success -> {
                    AppLogger.i(message = result.message)
                }
                is NotificationResult.Failure -> {
                    AppLogger.e(tag = "NotificationUseCase-SaveToken", message = result.exception.message ?: "Token save failed.", throwable = result.exception)
                }
            }
        } else {
            AppLogger.e(tag = "NotificationUseCase-TokenManager", message = "Failed to generate FCM token by Token manager.")
        }
    }

    suspend fun updateToken(userId: String, token: String) {
        when(val result = notificationRepository.updateToken(userId = userId, token = token)) {
            is NotificationResult.Success -> {
                AppLogger.i(message = result.message)
            }
            is NotificationResult.Failure -> {
                AppLogger.e(tag = "NotificationUseCase-UpdateToken", message = result.exception.message ?: "Token update failed.", throwable = result.exception)
            }
        }
    }
}