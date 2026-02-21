package com.example.localgrub.ui.screens.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel: ViewModel() {
    private val _phoneNumber = MutableStateFlow<String?>(null)
    val phoneNumber: StateFlow<String?> get() = _phoneNumber.asStateFlow()
    private val _verificationId = MutableStateFlow<String?>(null)
    val verificationId: StateFlow<String?> get() = _verificationId.asStateFlow()
    private val _token = MutableStateFlow<PhoneAuthProvider.ForceResendingToken?>(null)
    val token: StateFlow<PhoneAuthProvider.ForceResendingToken?> get() = _token.asStateFlow()
    private val _otpSentTime = MutableStateFlow(0L)
    val otpSentTime: StateFlow<Long> get() = _otpSentTime.asStateFlow()

    fun setInitialData(
        verificationId: String,
        token: PhoneAuthProvider.ForceResendingToken,
        otpSentTime: Long,
        phoneNumber: String? = null
    ) {
        _phoneNumber.value = phoneNumber
        _verificationId.value = verificationId
        _token.value = token
        _otpSentTime.value = otpSentTime
    }
}