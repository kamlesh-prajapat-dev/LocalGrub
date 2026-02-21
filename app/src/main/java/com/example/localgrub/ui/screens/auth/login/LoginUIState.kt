package com.example.localgrub.ui.screens.auth.login

sealed interface LoginUIState {
    object Idle: LoginUIState
    object Loading: LoginUIState
    data class Validation(val message: String): LoginUIState
    object NoInternet: LoginUIState
    data class OtpSent(val phoneNumber: String): LoginUIState
}