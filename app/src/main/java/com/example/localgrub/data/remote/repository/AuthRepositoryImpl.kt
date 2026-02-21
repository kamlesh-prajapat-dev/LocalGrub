package com.example.localgrub.data.remote.repository

import android.app.Activity
import com.example.localgrub.domain.mapper.toAuthError
import com.example.localgrub.domain.model.result.AuthResult
import com.example.localgrub.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {

    override fun sendVerificationCode(
        phoneNumber: String,
        activity: Activity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        require(phoneNumber.isNotBlank()) {
            "Phone number cannot be empty"
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override fun resendVerificationCode(
        phoneNumber: String,
        activity: Activity,
        token: PhoneAuthProvider.ForceResendingToken,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .setForceResendingToken(token)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override suspend fun signInWithPhoneAuthCredential(
        credential: PhoneAuthCredential
    ): AuthResult {
        return try {
            val authResult = auth
                .signInWithCredential(credential)
                .await()

            val isNewUser = authResult
                .additionalUserInfo
                ?.isNewUser
                ?: false

            AuthResult.Success(
                user = authResult.user,
                isNewUser = isNewUser
            )

        } catch (e: CancellationException) {
            // Very important: never swallow coroutine cancellation
            throw e

        } catch (e: Exception) {
            AuthResult.Failure(
                e.toAuthError()
            )
        }
    }

    override fun isUserLoggedIn(): Boolean {
        return getCurrentUser() != null
    }

    override fun logout() {
        auth.signOut()
    }

    override fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
}
