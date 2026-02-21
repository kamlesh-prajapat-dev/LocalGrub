package com.example.localgrub.domain.mapper

import com.example.localgrub.domain.model.failure.AuthError
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import java.io.IOException

fun Throwable.toAuthError(): AuthError {
    return when (this) {

        is FirebaseAuthInvalidCredentialsException -> {
            // Wrong OTP, expired OTP, malformed credential
            AuthError.InvalidOtp(this.message)
        }

        is FirebaseAuthInvalidUserException -> {
            AuthError.FirebaseError(errorCode, this.message)
        }

        is FirebaseAuthException -> {
            AuthError.FirebaseError(errorCode, this.message)
        }

        is IOException -> {
            // No internet, timeout, DNS failure, etc.
            AuthError.NetworkError
        }

        else -> {
            AuthError.Unknown(
                this
            )
        }
    }
}
