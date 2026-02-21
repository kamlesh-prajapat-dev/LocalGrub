package com.example.localgrub.ui.screens.auth.otp

import com.example.localgrub.domain.model.failure.AuthError
import com.example.localgrub.domain.model.failure.WriteReqDomainFailure
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

sealed interface OtpUIState {
    object Idle : OtpUIState
    object Loading : OtpUIState
    object NoInternet: OtpUIState
    data class OnVerificationCompleted(
        val credential: PhoneAuthCredential
    ): OtpUIState

    data class Validation(val message: String) : OtpUIState

    data class Success(val user: FirebaseUser?, val isNewUser: Boolean) : OtpUIState
    data class AuthFailure(val error: AuthError) : OtpUIState
    data class TokenUpdateSuccess(val isSuccess: Boolean) : OtpUIState
    data class TokenUpdateFailure(val failure: WriteReqDomainFailure): OtpUIState
    data class Verification(
        val verificationId: String,
        val token: PhoneAuthProvider.ForceResendingToken,
        val otpSentTime: Long,
        val message: String
    ) : OtpUIState
}