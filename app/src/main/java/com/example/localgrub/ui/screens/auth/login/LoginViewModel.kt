package com.example.localgrub.ui.screens.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localgrub.domain.usecase.UserUseCase
import com.example.localgrub.util.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val networkUtils: NetworkUtils,
    private val userUseCase: UserUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<LoginUIState>(LoginUIState.Idle)
    val uiState: StateFlow<LoginUIState> get() = _uiState.asStateFlow()

    fun loginUser(
        phoneNumber: String
    ) {
        _uiState.value = LoginUIState.Loading

        if(!networkUtils.isInternetAvailable()) {
            _uiState.value = LoginUIState.NoInternet
            return
        }

        val validationError = validateNumber(phoneNumber)

        if (validationError != null) {
            _uiState.value = LoginUIState.Validation(validationError)
            return
        }

//        _uiState.value = LoginUIState.OtpSent(phoneNumber)
        findUserByPhoneNumber(phoneNumber = phoneNumber)
    }

    private fun findUserByPhoneNumber(phoneNumber: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = userUseCase.getUserByPhoneNumber("+91$phoneNumber")
        }
    }

    private fun validateNumber(phoneNumber: String): String? {
        if (phoneNumber.isBlank()) return "Phone number must not be empty."
        if (phoneNumber.length != 10) return "Invalid phone number."
        return null
    }

    fun reset() {
        _uiState.value = LoginUIState.Idle
    }
}