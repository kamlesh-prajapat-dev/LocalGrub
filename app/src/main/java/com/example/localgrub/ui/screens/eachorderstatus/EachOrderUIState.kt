package com.example.localgrub.ui.screens.eachorderstatus

import com.example.localgrub.data.model.FetchedOrder
import com.example.localgrub.domain.mapper.firebase.GetReqDomainFailure
import com.example.localgrub.domain.mapper.firebase.WriteReqDomainFailure

sealed interface EachOrderUIState {
    object Idle: EachOrderUIState
    object Loading: EachOrderUIState
    data class Success(val isSuccess: Boolean): EachOrderUIState
    data class CancelOrderFailure(val failure: WriteReqDomainFailure): EachOrderUIState
    data class OrderGetFailure(val failure: GetReqDomainFailure): EachOrderUIState
    data class OrderGetSuccess(val order: FetchedOrder): EachOrderUIState
    object NoInternet: EachOrderUIState
}