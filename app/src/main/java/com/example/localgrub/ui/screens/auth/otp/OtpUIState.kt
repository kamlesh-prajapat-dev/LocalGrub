package com.example.localgrub.ui.screens.auth.otp

import com.example.localgrub.data.model.firebase.GetUser
import com.example.localgrub.data.model.api.response.OtpResponse
import com.example.localgrub.domain.model.failure.GetReqDomainFailure
import com.example.localgrub.domain.model.failure.WriteReqDomainFailure

sealed interface OtpUIState {
    object Idle : OtpUIState
    object Loading : OtpUIState
    object NoInternet: OtpUIState
    data class Validation(val message: String) : OtpUIState
    data class OtpSuccessfullySent(val response: OtpResponse, val otpSentTime: Long, val message: String): OtpUIState
    data class LoginSuccess(val user: GetUser, val isNewUser: Boolean) : OtpUIState
    data class Failure(val failure: GetReqDomainFailure) : OtpUIState
    object HomeState: OtpUIState
}