package com.example.roti999.data.model

import kotlinx.serialization.Serializable

@Serializable
data class NotificationRequest(
    val token: String,
    val title: String,
    val body: String,
    val orderId: String
)
