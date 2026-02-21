package com.example.localgrub.ui.screens.auth.otp

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localgrub.domain.mapper.toAuthError
import com.example.localgrub.domain.usecase.AuthUseCase
import com.example.localgrub.domain.usecase.TokenUseCase
import com.example.localgrub.util.AppConstant
import com.example.localgrub.util.NetworkUtils
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OtpViewModel @Inject constructor(
    private val authUseCase: AuthUseCase,
    private val tokenUseCase: TokenUseCase,
    private val networkUtils: NetworkUtils
) : ViewModel() {
    private val _uiState = MutableStateFlow<OtpUIState>(OtpUIState.Idle)
    val uiState: StateFlow<OtpUIState> get() = _uiState.asStateFlow()
    private val _phoneNumber = MutableStateFlow<String?>(null)
    val phoneNumber: StateFlow<String?> get() = _phoneNumber.asStateFlow()
    private val _verificationId = MutableStateFlow<String?>(null)
    val verificationId: StateFlow<String?> get() = _verificationId.asStateFlow()
    private val _token = MutableStateFlow<PhoneAuthProvider.ForceResendingToken?>(null)
    val token: StateFlow<PhoneAuthProvider.ForceResendingToken?> get() = _token.asStateFlow()
    private val _otpSentTime = MutableStateFlow(0L)
    val otpSentTime: StateFlow<Long> get() = _otpSentTime.asStateFlow()

    fun onSetOtpSentTime(otpSentTime: Long) {
        _otpSentTime.value = otpSentTime
    }

    private val _isTimerStart = MutableStateFlow(false)
    val isTimerStart: StateFlow<Boolean> get() = _isTimerStart.asStateFlow()

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            _uiState.value = OtpUIState.OnVerificationCompleted(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            _uiState.value = OtpUIState.AuthFailure(e.toAuthError())
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            _uiState.value = OtpUIState.Verification(
                verificationId = verificationId,
                token = token,
                otpSentTime = System.currentTimeMillis(),
                message = "OTP sent successfully."
            )
        }
    }

    fun setInitialData(
        verificationId: String,
        token: PhoneAuthProvider.ForceResendingToken,
        otpSentTime: Long
    ) {
        _verificationId.value = verificationId
        _token.value = token
        _otpSentTime.value = otpSentTime
    }

    fun sentOtp(
        activity: Activity,
        phoneNumber: String,
        oldPhoneNumber: String?
    ) {
        _uiState.value = OtpUIState.Loading

        _phoneNumber.value = phoneNumber
        _isTimerStart.value = false

        val otpSentTime = _otpSentTime.value
        if ((otpSentTime == 0L || System.currentTimeMillis() - otpSentTime >= AppConstant.OTP_VALIDITY_MS) && oldPhoneNumber != phoneNumber) {
            if (!networkUtils.isInternetAvailable()) {
                _uiState.value = OtpUIState.NoInternet
                return
            }

            sendVerificationCode(phoneNumber = phoneNumber, activity = activity)
        } else {
            _uiState.value = OtpUIState.Verification(
                verificationId = _verificationId.value ?: "",
                token = _token.value ?: PhoneAuthProvider.ForceResendingToken.zza(),
                otpSentTime = otpSentTime,
                message = "OTP already sent. Please wait before requesting again."
            )
        }
    }

    private fun sendVerificationCode(activity: Activity, phoneNumber: String) {
        val fullPhoneNumber = "+91$phoneNumber"
        authUseCase.sendVerificationCode(fullPhoneNumber, activity, callbacks)
    }

    fun verifyOtp(otp: String) {
        _uiState.value = OtpUIState.Loading

        val validationError = validateOtp(otp)

        if (validationError != null) {
            _uiState.value = OtpUIState.Validation(validationError)
            return
        }

        if (!networkUtils.isInternetAvailable()) {
            _uiState.value = OtpUIState.NoInternet
            return
        }

        val verificationId = _verificationId.value
        if (verificationId != null) {
            val credential = PhoneAuthProvider.getCredential(verificationId, otp)
            signInWithPhoneAuthCredential(credential)
        } else {
            _uiState.value = OtpUIState.Validation(
                "Invalid Otp."
            )
        }
    }

    fun verifyOtp(credential: PhoneAuthCredential) {
        _uiState.value = OtpUIState.Loading

        if (!networkUtils.isInternetAvailable()) {
            _uiState.value = OtpUIState.NoInternet
            return
        }

        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = authUseCase.singInWithPhoneAuthCredential(credential)
        }
    }

    private fun validateOtp(otp: String): String? {
        if (otp.isBlank()) return "Otp must not be blank."
        if (otp.length != 6) return "Invalid Otp."
        return null
    }

    // Resend Otp
    fun resendOtp(
        token: PhoneAuthProvider.ForceResendingToken,
        phoneNumber: String,
        activity: Activity
    ) {
        _uiState.value = OtpUIState.Loading

        if (!networkUtils.isInternetAvailable()) {
            _uiState.value = OtpUIState.NoInternet
            return
        }

        _isTimerStart.value = true

        val fullPhoneNumber = "+91$phoneNumber"
        authUseCase.resendVerificationCode(fullPhoneNumber, activity = activity, token, callbacks)
    }

    fun saveUserToken(userId: String) {
        _uiState.value = OtpUIState.Loading

        if (!networkUtils.isInternetAvailable()) {
            _uiState.value = OtpUIState.NoInternet
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = tokenUseCase.saveUserToken(userId)
        }
    }

    fun reset() {
        _uiState.value = OtpUIState.Idle
        _phoneNumber.value = null
        _verificationId.value = null
        _token.value = null
    }
}