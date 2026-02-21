package com.example.localgrub.ui.screens.createprofile

import com.example.localgrub.data.model.GetUser
import com.example.localgrub.domain.model.failure.WriteReqDomainFailure

sealed interface ProfileUIState {
    object Idle : ProfileUIState
    object Loading : ProfileUIState
    data class Success(val user: GetUser): ProfileUIState
    data class Failure(val failure: WriteReqDomainFailure) : ProfileUIState
    data class ValidationErrors(val msgForName: String?, val msgForAddress: String?, val noneToSave: String?) : ProfileUIState
    object NoInternet: ProfileUIState
}