package com.example.localgrub.domain.model.result

import com.example.localgrub.domain.model.failure.AuthError
import com.google.firebase.auth.FirebaseUser

sealed class AuthResult {
    data class Success(
        val user: FirebaseUser?,
        val isNewUser: Boolean
    ) : AuthResult()

    data class Failure(
        val error: AuthError,
        val throwable: Throwable? = null
    ) : AuthResult()
}