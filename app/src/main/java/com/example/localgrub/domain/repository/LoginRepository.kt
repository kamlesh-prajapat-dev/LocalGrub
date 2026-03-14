package com.example.localgrub.domain.repository

import com.example.localgrub.data.model.api.request.OtpRequest
import com.example.localgrub.data.model.api.request.ResendOtpRequest
import com.example.localgrub.data.model.api.request.VerifyOtpRequest
import com.example.localgrub.domain.model.result.LoginResult

interface LoginRepository {
    suspend fun sendOtp(request: OtpRequest): LoginResult
    suspend fun verifyOtp(request: VerifyOtpRequest): LoginResult
    suspend fun resendOtp(request: ResendOtpRequest): LoginResult
}