package com.example.roti999.data.repository

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
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val networkUtils: NetworkUtils
) : AuthRepository {

    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    override val authState: StateFlow<AuthUiState> get() = _authState.asStateFlow()

    private  val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            _authState.value = AuthUiState.Success
        }

        override fun onVerificationFailed(e: FirebaseException) {
            _authState.value = AuthUiState.Error(e.message ?: "An unknown error occurred")
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            _authState.value = AuthUiState.OtpSent(verificationId)
        }
    }

    override suspend fun sendOtp(phoneNumber: String, activity: Activity) {
        _authState.value = AuthUiState.Loading
        if (networkUtils.isInternetAvailable()) {
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber("+91$phoneNumber") // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(activity) // Activity (for callback binding)
                .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        } else {
            _authState.value = AuthUiState.IsNetworkAvailable
        }
    }

    override suspend fun verifyOtp(otp: String, verificationId: String) {
        _authState.value = AuthUiState.Loading
        if (networkUtils.isInternetAvailable()) {
            val credential = PhoneAuthProvider.getCredential(verificationId, otp)
            signInWithPhoneAuthCredential(credential)
        } else {
            _authState.value = AuthUiState.IsNetworkAvailable
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthUiState.Success
                } else {
                    _authState.value = AuthUiState.Error(task.exception?.message ?: "An unknown error occurred")
                }
            }
    }

    override fun resetState() {
        _authState.value = AuthUiState.Idle
    }
}
