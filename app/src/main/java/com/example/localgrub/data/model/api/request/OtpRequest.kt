package com.example.localgrub.data.model.api.request

import kotlinx.serialization.Serializable

@Serializable
data class OtpRequest(
    val phoneNumber: String
)
