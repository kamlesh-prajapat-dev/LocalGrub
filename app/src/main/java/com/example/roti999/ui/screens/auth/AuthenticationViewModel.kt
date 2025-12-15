package com.example.roti999.ui.screens.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roti999.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    val authState: StateFlow<AuthUiState> = authRepository.authState

    private val _uiEvent = MutableSharedFlow<AuthUIEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onSetUIEvent(event: AuthUIEvent) {
        viewModelScope.launch {
            _uiEvent.emit(event)
        }
    }
    fun sendOtp(phoneNumber: String, activity: Activity) {
        viewModelScope.launch(Dispatchers.IO) {
            authRepository.sendOtp(phoneNumber, activity)
        }
    }

    fun verifyOtp(otp: String, verificationId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            authRepository.verifyOtp(otp, verificationId)
        }
    }

    fun resetState() {
        authRepository.resetState()
    }
}
