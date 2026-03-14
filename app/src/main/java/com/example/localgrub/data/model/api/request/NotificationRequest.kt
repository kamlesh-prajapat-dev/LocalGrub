package com.example.localgrub.data.model.api.request

import kotlinx.serialization.Serializable

@Serializable
data class NotificationRequest(
    val userId: String,
    val orderId: String,
    val status: String,
    val userName: String
)