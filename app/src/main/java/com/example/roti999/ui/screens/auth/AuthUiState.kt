package com.example.roti999.ui.screens.auth

import java.lang.Exception

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object OtpLayout: AuthUiState()
    data class OtpSent(val verificationId: String) : AuthUiState()
    object Success : AuthUiState()
    data class AuthFailure(val e: Exception?) : AuthUiState()

    // Phone number input field - true and otp input field - false
    data class ValidationError(val message: String, val field: Boolean) : AuthUiState()
    object NoInternet : AuthUiState()
}