package com.example.localgrub.ui.screens.profilebuilder

import com.example.localgrub.data.model.firebase.GetUser
import com.example.localgrub.domain.model.failure.WriteReqDomainFailure

sealed interface ProfileBuilderUIState {
    object Idle : ProfileBuilderUIState
    object Loading : ProfileBuilderUIState
    data class Success(val user: GetUser): ProfileBuilderUIState
    data class Failure(val failure: WriteReqDomainFailure) : ProfileBuilderUIState
    data class ValidationErrors(val msgForName: String?, val msgForAddress: String?, val noneToSave: String?) : ProfileBuilderUIState
    object NoInternet: ProfileBuilderUIState
}