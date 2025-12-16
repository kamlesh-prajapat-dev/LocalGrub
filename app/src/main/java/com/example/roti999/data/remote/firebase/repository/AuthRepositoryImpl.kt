package com.example.roti999.data.remote.firebase.repository

import android.app.Activity
import com.example.roti999.domain.repository.AuthRepository
import com.example.roti999.ui.screens.auth.AuthUiState
import com.example.roti999.util.NetworkUtils
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val networkUtils: NetworkUtils
) : AuthRepository {

    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    override val authState: StateFlow<AuthUiState> get() = _authState.asStateFlow()

    private val _verificationId = MutableStateFlow<String?>(null)
    override val verificationId: StateFlow<String?> get() = _verificationId.asStateFlow()

    private var token: PhoneAuthProvider.ForceResendingToken? = null

    private fun createCallbacks(): PhoneAuthProvider.OnVerificationStateChangedCallbacks {
        return object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                _authState.update { AuthUiState.Success }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _authState.update { AuthUiState.AuthFailure(e) }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                _verificationId.update { verificationId }
                this@AuthRepositoryImpl.token = token
                _authState.update { AuthUiState.OtpSent }
            }
        }
    }

    override suspend fun sendOtp(phoneNumber: String, activity: Activity) {
        _authState.value = AuthUiState.Loading

        val validationResult = validatePhoneNumber(phoneNumber)
        if (validationResult != null) {
            _authState.update { AuthUiState.ValidationError(validationResult, true) }
            return
        }

        if (!networkUtils.isInternetAvailable()) {
            _authState.update { AuthUiState.NoInternet }
            return
        }

        _authState.update { AuthUiState.OtpLayout }

        val callbacks = createCallbacks()

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+91$phoneNumber") // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(activity) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override suspend fun verifyOtp(otp: String, verificationId: String) {
        _authState.update {  AuthUiState.Loading }

        val validationResult = validateOtp(otp)
        if (validationResult != null) {
            _authState.update { AuthUiState.ValidationError(validationResult, false) }
            return
        }

        if (!networkUtils.isInternetAvailable()) {
            _authState.update { AuthUiState.NoInternet }
            return
        }

        val credential = PhoneAuthProvider.getCredential(verificationId, otp)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.update { AuthUiState.Success }
                } else {
                    _authState.update { AuthUiState.AuthFailure(task.exception) }
                }
            }
    }

    override fun resetState() {
        _authState.update { AuthUiState.Idle }
        _verificationId.update { null }
    }

    private fun validatePhoneNumber(phoneNumber: String): String? {
        if (phoneNumber.isBlank()) {
            return "Phone number must not be null"
        }

        if (phoneNumber.length != 10) {
            return "Please enter a valid 10-digit phone number"
        }

        return null
    }

    private fun validateOtp(otp: String): String? {
        if (otp.isBlank()) {
            return "OTP must not be null"
        }

        if (otp.length != 6) {
            return "Please enter a valid 6-digit OTP"
        }

        return null
    }
}
