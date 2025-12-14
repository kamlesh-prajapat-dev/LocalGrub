package com.example.roti999.ui.screens.auth

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class OtpSent(val verificationId: String) : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
    object IsNetworkAvailable : AuthUiState()
}