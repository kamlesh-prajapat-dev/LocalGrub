package com.example.localgrub.ui.screens.auth.login

import com.example.localgrub.data.model.api.response.OtpResponse
import com.example.localgrub.domain.model.failure.GetReqDomainFailure

sealed interface LoginUIState {
    object Idle: LoginUIState
    object Loading: LoginUIState
    data class Validation(val message: String): LoginUIState
    object NoInternet: LoginUIState
    data class OtpSentSuccessfully(
        val phoneNumber: String,
        val response: OtpResponse,
        val currentTimeMillis: Long,
        val string: String
    ): LoginUIState
    data class Failure(val failure: GetReqDomainFailure): LoginUIState
}