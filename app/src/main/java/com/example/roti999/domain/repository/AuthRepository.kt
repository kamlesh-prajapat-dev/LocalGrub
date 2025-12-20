package com.example.roti999.domain.repository

import android.app.Activity
import com.example.roti999.ui.screens.auth.AuthUiState
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val authState: StateFlow<AuthUiState>
    val verificationId: StateFlow<String?>
    suspend fun sendOtp(phoneNumber: String, activity: Activity)
    suspend fun resendOtp(phoneNumber: String, activity: Activity)
    suspend fun verifyOtp(otp: String, verificationId: String)
    fun resetState()
}
