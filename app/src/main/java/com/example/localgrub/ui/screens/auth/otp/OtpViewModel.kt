package com.example.localgrub.ui.screens.auth.otp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localgrub.data.model.api.response.OtpResponse
import com.example.localgrub.domain.usecase.LoginUseCase
import com.example.localgrub.domain.usecase.NotificationUseCase
import com.example.localgrub.util.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OtpViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val notificationUseCase: NotificationUseCase,
    private val networkUtils: NetworkUtils
) : ViewModel() {
    private val _uiState = MutableStateFlow<OtpUIState>(OtpUIState.Idle)
    val uiState: StateFlow<OtpUIState> get() = _uiState.asStateFlow()
    private val _phoneNumber = MutableStateFlow<String?>(null)
    val phoneNumber: StateFlow<String?> get() = _phoneNumber.asStateFlow()
    private val _response = MutableStateFlow<OtpResponse?>(null)
    val response: StateFlow<OtpResponse?> get() = _response.asStateFlow()
    private val _otpSentTime = MutableStateFlow(0L)
    val otpSentTime: StateFlow<Long> get() = _otpSentTime.asStateFlow()
    fun onSetOtpSentTime(otpSentTime: Long) {
        _otpSentTime.value = otpSentTime
    }

    private val _isTimerStart = MutableStateFlow(false)
    val isTimerStart: StateFlow<Boolean> get() = _isTimerStart.asStateFlow()

    fun setInitialData(
        response: OtpResponse,
        otpSentTime: Long,
        phoneNumber: String
    ) {
        _phoneNumber.value = phoneNumber
        _response.value = response
        _otpSentTime.value = otpSentTime
    }

    fun setInitialData(
        response: OtpResponse,
        otpSentTime: Long
    ) {
        _response.value = response
        _otpSentTime.value = otpSentTime
    }

    fun verifyOtp(otp: String) {
        _uiState.value = OtpUIState.Loading

        val validationError = validateOtp(otp)

        if (validationError != null) {
            _uiState.value = OtpUIState.Validation(validationError)
            return
        }

        if (!networkUtils.hasInternetAccess()) {
            _uiState.value = OtpUIState.NoInternet
            return
        }

        val response = _response.value
        if (response != null && response.message.isNotBlank()) {
            viewModelScope.launch(Dispatchers.IO) {
                _uiState.value = loginUseCase.verifyOtp(
                    phoneNumber = _phoneNumber.value ?: "",
                    otp = otp,
                    requestId = response.message
                )
            }
        } else {
            _uiState.value = OtpUIState.Validation(
                "Invalid Otp."
            )
        }
    }

    private fun validateOtp(otp: String): String? {
        if (otp.isBlank()) return "Otp must not be blank."
        if (otp.length != 6) return "Invalid Otp."
        return null
    }

    fun resendOtp(
        phoneNumber: String,
        response: OtpResponse,
    ) {
        _uiState.value = OtpUIState.Loading

        if (!networkUtils.hasInternetAccess()) {
            _uiState.value = OtpUIState.NoInternet
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = loginUseCase.resendOtp(
                phoneNumber = phoneNumber,
                requestId = response.message
            )
        }

        _isTimerStart.value = true
    }

    fun saveUserToken(userId: String) {
        _uiState.value = OtpUIState.Loading

        if (!networkUtils.hasInternetAccess()) {
            _uiState.value = OtpUIState.NoInternet
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            notificationUseCase.saveToken(userId = userId)
            _uiState.value = OtpUIState.HomeState
        }
    }

    fun reset() {
        _uiState.value = OtpUIState.Idle
        _phoneNumber.value = null
        _response.value = null
    }
}