package com.example.roti999.ui.screens.splash

sealed interface SplashNavigationState {
    object Idle: SplashNavigationState
    object Home : SplashNavigationState
    object Authentication : SplashNavigationState
}