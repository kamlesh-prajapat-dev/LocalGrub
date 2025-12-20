package com.example.roti999.data.remote.firebase.repository

import android.app.Activity
import com.example.roti999.domain.repository.AuthRepository
import com.example.roti999.ui.screens.auth.AuthUiState
import com.example.roti999.util.NetworkUtils
import com.example.roti999.util.Validator
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

private const val COUNTRY_CODE = "+91"

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val networkUtils: NetworkUtils,
    private val validator: Validator
) : AuthRepository {

    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    override val authState: StateFlow<AuthUiState> get() = _authState.asStateFlow()

    private val _verificationId = MutableStateFlow<String?>(null)
    override val verificationId: StateFlow<String?> get() = _verificationId.asStateFlow()

    private var token: PhoneAuthProvider.ForceResendingToken? = null

    private fun createCallbacks(): PhoneAuthProvider.OnVerificationStateChangedCallbacks {
        return object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(credential)
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
        _verificationId.value = null
        _authState.value = AuthUiState.Loading

        val validationResult = validator.validatePhoneNumber(phoneNumber)
        if (validationResult != null) {
            _authState.value = AuthUiState.ValidationError(validationResult, true)
            return
        }

        if (!networkUtils.isInternetAvailable()) {
            _authState.value =  AuthUiState.NoInternet
            return
        }

        _authState.value = AuthUiState.OtpLayout

        val callbacks = createCallbacks()

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("$COUNTRY_CODE$phoneNumber") // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(activity) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override suspend fun resendOtp(phoneNumber: String, activity: Activity) {
        _verificationId.value = null
        _authState.value = AuthUiState.Loading

        if (!networkUtils.isInternetAvailable()) {
            _authState.value =  AuthUiState.NoInternet
            return
        }

        val resendToken = token
        if (resendToken == null) {
            // If token is not available, treat as a first-time send
            sendOtp(phoneNumber, activity)
            return
        }

        val callbacks = createCallbacks()

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("$COUNTRY_CODE$phoneNumber")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .setForceResendingToken(resendToken)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override suspend fun verifyOtp(otp: String, verificationId: String) {
        _authState.value = AuthUiState.Loading

        val validationResult = validator.validateOtp(otp)
        if (validationResult != null) {
            _authState.value =  AuthUiState.ValidationError(validationResult, false)
            return
        }

        if (!networkUtils.isInternetAvailable()) {
            _authState.value = AuthUiState.NoInternet
            return
        }

        val credential = PhoneAuthProvider.getCredential(verificationId, otp)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthUiState.Success
                } else {
                    _authState.value = AuthUiState.AuthFailure(task.exception)
                }
            }
    }

    override fun resetState() {
        _authState.value =  AuthUiState.Idle
        _verificationId.value =  null
    }
}
