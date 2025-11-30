package com.example.roti999.ui.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roti999.data.database.local.LocalDatabase
import com.example.roti999.domain.repository.AuthRepository
import com.example.roti999.domain.model.AuthUiState
import com.example.roti999.util.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val networkUtils: NetworkUtils
) : ViewModel() {
    val authState: StateFlow<AuthUiState> = authRepository.authState

    fun sendOtp(phoneNumber: String, activity: Activity) {
        viewModelScope.launch {
            authRepository.sendOtp(phoneNumber, activity)
        }
    }

    fun verifyOtp(otp: String, verificationId: String) {
        viewModelScope.launch {
            authRepository.verifyOtp(otp, verificationId)
        }
    }

    fun resetState() {
        authRepository.resetState()
    }

    // Check Internet is available
    fun isInternetAvailable(): Boolean = networkUtils.isInternetAvailable()
}
