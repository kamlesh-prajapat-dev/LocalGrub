package com.example.roti999.domain.repository

import android.app.Activity
import com.example.roti999.domain.model.AuthUiState
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val authState: StateFlow<AuthUiState>
    suspend fun sendOtp(phoneNumber: String, activity: Activity)
    suspend fun verifyOtp(otp: String, verificationId: String)
    fun resetState()
}
