package com.example.localgrub.data.remote.repository

import com.example.localgrub.data.model.api.request.NotificationRequest
import com.example.localgrub.data.model.api.request.TokenRequest
import com.example.localgrub.data.remote.api.NotificationApiService
import com.example.localgrub.domain.model.result.NotificationResult
import com.example.localgrub.domain.repository.NotificationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val apiService: NotificationApiService
) : NotificationRepository {

    override suspend fun sendNotification(notificationRequest: NotificationRequest): NotificationResult {
        return try {
            val response = apiService.sendNotification(notificationRequest)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success) {
                    NotificationResult.Success(apiResponse.data ?: apiResponse.message)
                } else {
                    NotificationResult.Failure(Exception(apiResponse.message))
                }
            } else {
                NotificationResult.Failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            NotificationResult.Failure(e)
        }
    }

    override suspend fun saveToken(userId: String, token: String): NotificationResult {
        return try {
            val response = apiService.saveToken(userId, TokenRequest(token, "android"))
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success) {
                    NotificationResult.Success(apiResponse.data ?: apiResponse.message)
                } else {
                    NotificationResult.Failure(Exception(apiResponse.message))
                }
            } else {
                NotificationResult.Failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            NotificationResult.Failure(e)
        }
    }

    override suspend fun updateToken(userId: String, token: String): NotificationResult {
        return try {
            val response = apiService.updateToken(userId, token)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success) {
                    NotificationResult.Success(apiResponse.data ?: apiResponse.message)
                } else {
                    NotificationResult.Failure(Exception(apiResponse.message))
                }
            } else {
                NotificationResult.Failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            NotificationResult.Failure(e)
        }
    }

    override suspend fun deleteToken(userId: String): NotificationResult {
        return try {
            val response = apiService.deleteToken(userId)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success) {
                    NotificationResult.Success(apiResponse.data ?: apiResponse.message)
                } else {
                    NotificationResult.Failure(Exception(apiResponse.message))
                }
            } else {
                NotificationResult.Failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            NotificationResult.Failure(e)
        }
    }
}
