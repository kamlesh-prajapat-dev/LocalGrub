package com.example.roti999.domain.usecase

import android.app.Activity
import com.example.roti999.domain.repository.AuthRepository
import javax.inject.Inject

class AuthUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    val authState = authRepository.authState
    val verificationId = authRepository.verificationId
    fun resetState() = authRepository.resetState()

    suspend fun sendOtp(phoneNumber: String, activity: Activity) {
        authRepository.sendOtp(phoneNumber, activity)
    }

    suspend fun resendOtp(phoneNumber: String, activity: Activity) {
        authRepository.resendOtp(phoneNumber, activity)
    }

    suspend fun verifyOtp(otp: String, verificationId: String) {
        authRepository.verifyOtp(otp, verificationId)
    }
}
