package com.example.roti999.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {
    private val _uiState = MutableStateFlow<SplashNavigationState>(SplashNavigationState.Idle)
    val uiState: StateFlow<SplashNavigationState> get() = _uiState.asStateFlow()

    init {
        checkUserLoggedIn()
    }
    private fun checkUserLoggedIn() {
        viewModelScope.launch {
            _uiState.value = if (auth.currentUser != null) {
                SplashNavigationState.Home
            } else {
                SplashNavigationState.Authentication
            }
        }
    }
}