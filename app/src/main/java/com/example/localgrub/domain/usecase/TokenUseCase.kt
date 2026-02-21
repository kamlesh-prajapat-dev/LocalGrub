package com.example.localgrub.domain.usecase

import com.example.localgrub.domain.mapper.firestore.FirestoreWriteFailureMapper
import com.example.localgrub.domain.model.result.TokenResult
import com.example.localgrub.domain.repository.TokenRepository
import com.example.localgrub.ui.screens.auth.otp.OtpUIState
import com.example.localgrub.util.TokenManager
import javax.inject.Inject
import javax.inject.Singleton

class TokenUseCase @Inject constructor(
    private val tokenRepository: TokenRepository
) {

    private suspend fun getFcmToken(): String? = TokenManager.getFCMToken()

    suspend fun saveUserToken(userId: String): OtpUIState {
        val token = getFcmToken()
        return if (token != null) {
            when (val result = tokenRepository.saveToken(token, userId)) {
                is TokenResult.TokenUpdateSuccess -> {
                    OtpUIState.TokenUpdateSuccess(result.success)
                }

                is TokenResult.Failure -> {
                    OtpUIState.TokenUpdateFailure(
                        FirestoreWriteFailureMapper.map(
                            result.exception,
                            userId
                        )
                    )
                }

                else -> OtpUIState.Idle
            }
        } else {
            OtpUIState.TokenUpdateFailure(
                FirestoreWriteFailureMapper.map(
                    IllegalArgumentException("FCM token cannot be empty"),
                    userId
                )
            )
        }
    }

    suspend fun saveUserToken(token: String, userId: String): OtpUIState {
        return when (val result = tokenRepository.saveToken(token = token, docId = userId)) {
            is TokenResult.TokenUpdateSuccess -> {
                OtpUIState.TokenUpdateSuccess(result.success)
            }

            is TokenResult.Failure -> {
                OtpUIState.TokenUpdateFailure(
                    FirestoreWriteFailureMapper.map(
                        result.exception,
                        userId
                    )
                )
            }

            else -> OtpUIState.Idle
        }
    }
}