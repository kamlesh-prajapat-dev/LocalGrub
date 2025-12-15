package com.example.roti999.ui.screens.auth

sealed class AuthUIEvent {

    data class ShowToast(val message: String) : AuthUIEvent()
    object NavigateToHome : AuthUIEvent()
    object ShowNoInternetDialog : AuthUIEvent()
}