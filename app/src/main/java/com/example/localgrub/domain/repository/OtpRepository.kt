package com.example.localgrub.domain.repository

import com.example.localgrub.domain.model.result.LoginResult

interface OtpRepository {
    suspend fun sendOtp(phoneNumber: String): LoginResult
}