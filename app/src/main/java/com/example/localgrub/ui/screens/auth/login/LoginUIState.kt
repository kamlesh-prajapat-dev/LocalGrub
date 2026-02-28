package com.example.localgrub.ui.screens.auth.login

import com.example.localgrub.domain.model.failure.GetReqDomainFailure

sealed interface LoginUIState {
    object Idle: LoginUIState
    object Loading: LoginUIState
    data class Validation(val message: String): LoginUIState
    object NoInternet: LoginUIState
    data class OtpSent(val phoneNumber: String): LoginUIState
    object HomeState: LoginUIState
    data class UserGetFailure(val failure: GetReqDomainFailure): LoginUIState
}