package com.example.roti999.ui.screens.home

sealed class HomeUIEvent {
    data class ShowToast(val message: String) : HomeUIEvent()
    object ShowNoInternetDialog : HomeUIEvent()
}