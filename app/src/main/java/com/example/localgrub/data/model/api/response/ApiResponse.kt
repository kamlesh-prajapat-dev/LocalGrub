package com.example.localgrub.data.model.api.response

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse <T> (
    val success: Boolean = false,
    val message: String = "",
    val data: T? = null
)