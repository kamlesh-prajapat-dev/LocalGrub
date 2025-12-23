package com.example.roti999.domain.model

sealed interface NotificationResult {
    data class Success(val isSuccess: Boolean = false): NotificationResult
    data class Error(val e: Exception): NotificationResult
}