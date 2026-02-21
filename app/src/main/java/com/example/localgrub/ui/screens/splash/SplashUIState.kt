package com.example.localgrub.ui.screens.splash

sealed interface SplashUIState {
    object Idle: SplashUIState
    object Home : SplashUIState
    object Authentication : SplashUIState
}