package com.example.localgrub.data.model.api.request

import kotlinx.serialization.Serializable

@Serializable
data class VerifyOtpRequest(
    val phoneNumber: String,
    val otp: String,
    val requestId: String
)
