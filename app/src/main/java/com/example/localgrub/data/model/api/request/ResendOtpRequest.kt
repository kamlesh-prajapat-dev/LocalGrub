package com.example.localgrub.data.model.api.request

import kotlinx.serialization.Serializable

@Serializable
data class ResendOtpRequest(
    val phoneNumber: String,
    val requestId: String
)