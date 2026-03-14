package com.example.localgrub.ui.screens.auth

import androidx.lifecycle.ViewModel
import com.example.localgrub.data.model.api.response.OtpResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel: ViewModel() {
    private val _phoneNumber = MutableStateFlow<String?>(null)
    val phoneNumber: StateFlow<String?> get() = _phoneNumber.asStateFlow()

    private val _response = MutableStateFlow<OtpResponse?>(null)
    val response: StateFlow<OtpResponse?> get() = _response.asStateFlow()

    private val _otpSentTime = MutableStateFlow(0L)
    val otpSentTime: StateFlow<Long> get() = _otpSentTime.asStateFlow()

    fun setInitialData(
        response: OtpResponse,
        otpSentTime: Long,
        phoneNumber: String? = null
    ) {
        _phoneNumber.value = phoneNumber
        _response.value = response
        _otpSentTime.value = otpSentTime
    }
}