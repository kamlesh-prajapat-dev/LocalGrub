package com.example.localgrub.data.model.api.response

import kotlinx.serialization.Serializable

@Serializable
data class OtpResponse(
    val message: String = "",
    val type: String = ""
)
