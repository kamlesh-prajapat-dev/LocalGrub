package com.example.localgrub.domain.usecase

import com.example.localgrub.data.local.LocalDatabase
import com.example.localgrub.data.model.api.request.OtpRequest
import com.example.localgrub.data.model.api.request.ResendOtpRequest
import com.example.localgrub.data.model.api.request.VerifyOtpRequest
import com.example.localgrub.domain.mapper.firestore.FirestoreFailureMapper
import com.example.localgrub.domain.model.failure.GetReqDomainFailure
import com.example.localgrub.domain.model.result.LoginResult
import com.example.localgrub.domain.model.result.NotificationResult
import com.example.localgrub.domain.repository.LoginRepository
import com.example.localgrub.domain.repository.NotificationRepository
import com.example.localgrub.domain.repository.OtpRepository
import com.example.localgrub.ui.screens.auth.login.LoginUIState
import com.example.localgrub.ui.screens.auth.otp.OtpUIState
import com.example.localgrub.ui.screens.home.HomeUIState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginUseCase @Inject constructor(
    private val loginRepository: LoginRepository,
    private val otpRepository: OtpRepository,
    private val notificationRepository: NotificationRepository,
    private val localDatabase: LocalDatabase
) {
    suspend fun sendOtp(phoneNumber: String): LoginUIState {
        return when (val result = otpRepository.sendOtp(phoneNumber = phoneNumber)) {
            is LoginResult.SendOtpSuccess -> LoginUIState.OtpSentSuccessfully(
                phoneNumber = phoneNumber,
                result.response,
                System.currentTimeMillis(),
                "OTP sent successfully."
            )

            is LoginResult.Failure -> {
                LoginUIState.Failure(result.failure)
            }

            else -> LoginUIState.Idle
        }
    }

    suspend fun verifyOtp(phoneNumber: String, otp: String, requestId: String): OtpUIState {
        return when (val result = loginRepository.verifyOtp(
            VerifyOtpRequest(
                phoneNumber = phoneNumber,
                otp = otp,
                requestId = requestId
            )
        )) {
            is LoginResult.VerifyOtpSuccess -> {
                val user = result.response.user
                localDatabase.setToken(token = result.response.token)
                localDatabase.setUser(user = user)
                OtpUIState.LoginSuccess(user = user, isNewUser = result.response.newUser)
            }

            is LoginResult.Failure -> {
                OtpUIState.Failure(result.failure)
            }

            else -> OtpUIState.Idle
        }
    }

    suspend fun resendOtp(phoneNumber: String, requestId: String): OtpUIState {
        return when (val result = loginRepository.resendOtp(
            ResendOtpRequest(
                phoneNumber = "91$phoneNumber",
                requestId = requestId
            )
        )) {
            is LoginResult.SendOtpSuccess -> OtpUIState.OtpSuccessfullySent(
                result.response,
                System.currentTimeMillis(),
                "OTP Resent successfully."
            )

            is LoginResult.Failure -> {
                OtpUIState.Failure(result.failure)
            }

            else -> OtpUIState.Idle
        }
    }

    suspend fun logout(userId: String): HomeUIState {
        return when (val result = notificationRepository.deleteToken(userId = userId)) {
            is NotificationResult.Success -> {
                localDatabase.setToken(null)
                localDatabase.setUser(null)
                HomeUIState.LoginState
            }

            is NotificationResult.Failure -> {
                HomeUIState.FirestoreGetFailure(
                    FirestoreFailureMapper.map(
                        result.exception,
                        userId
                    )
                )
            }
        }
    }

    fun getToken(): String? {
        return localDatabase.getToken()
    }
}
