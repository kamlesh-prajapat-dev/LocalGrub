package com.example.localgrub.domain.repository

import android.app.Activity
import com.example.localgrub.domain.model.result.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

interface AuthRepository {
    fun sendVerificationCode(
        phoneNumber: String,
        activity: Activity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    )

    fun resendVerificationCode(
        phoneNumber: String,
        activity: Activity,
        token: PhoneAuthProvider.ForceResendingToken,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    )

    suspend fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential): AuthResult

    fun isUserLoggedIn(): Boolean
    fun logout()

    fun getCurrentUser(): FirebaseUser?
}
