package com.example.localgrub.domain.usecase

import android.app.Activity
import com.example.localgrub.domain.model.result.AuthResult
import com.example.localgrub.domain.repository.AuthRepository
import com.example.localgrub.ui.screens.auth.otp.OtpUIState
import com.example.localgrub.util.NetworkUtils
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import javax.inject.Inject

class AuthUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    fun sendVerificationCode(phoneNumber: String, activity: Activity, callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks) {
        authRepository.sendVerificationCode(phoneNumber, activity, callbacks)
    }

    fun resendVerificationCode(phoneNumber: String, activity: Activity, token: PhoneAuthProvider.ForceResendingToken, callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks) {
        authRepository.resendVerificationCode(phoneNumber, activity, token, callbacks)
    }

    suspend fun singInWithPhoneAuthCredential(credential: PhoneAuthCredential): OtpUIState {
        return when(val result = authRepository.signInWithPhoneAuthCredential(credential)) {
            is AuthResult.Success -> {
                OtpUIState.Success(result.user, result.isNewUser)
            }

            is AuthResult.Failure -> {
                OtpUIState.AuthFailure(result.error)
            }
        }
    }

    fun logout() = authRepository.logout()

    fun getCurrentUser() = authRepository.getCurrentUser()
}
