package com.example.localgrub.domain.model.failure

sealed class AuthError {
    data class InvalidOtp(val message: String?) : AuthError()
    data class FirebaseError(
        val code: String?,
        val message: String?
    ) : AuthError()
    object NetworkError : AuthError()
    data class Unknown(
        val throwable: Throwable? = null
    ) : AuthError()
}
