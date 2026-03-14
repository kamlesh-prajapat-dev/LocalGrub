package com.example.localgrub.data.model.api.response

import com.example.localgrub.data.model.firebase.GetUser
import kotlinx.serialization.Serializable

@Serializable
data class VerifyOtpResponse (
    val token: String = "",
    val newUser: Boolean = false,
    val user: GetUser = GetUser()
)