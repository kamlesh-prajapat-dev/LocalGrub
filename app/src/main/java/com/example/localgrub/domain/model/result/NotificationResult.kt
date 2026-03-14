package com.example.localgrub.domain.model.result

sealed interface NotificationResult {
    data class Success(val message: String) : NotificationResult
    data class Failure(val exception: Exception) : NotificationResult
}