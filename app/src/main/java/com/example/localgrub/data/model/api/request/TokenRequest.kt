package com.example.localgrub.data.model.api.request

import kotlinx.serialization.Serializable

@Serializable
data class TokenRequest(
    val token: String,
    val platform: String
)