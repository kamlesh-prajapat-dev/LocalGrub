package com.example.localgrub.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localgrub.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<SplashUIState>(SplashUIState.Idle)
    val uiState: StateFlow<SplashUIState> get() = _uiState.asStateFlow()

    fun isUserLoggedIn() {
        viewModelScope.launch {
            _uiState.value = if (loginUseCase.getToken() != null) {
                delay(2000)
                SplashUIState.Home
            } else {
                delay(2000)
                SplashUIState.Authentication
            }
        }
    }

    fun reset() {
        _uiState.value = SplashUIState.Idle
    }
}