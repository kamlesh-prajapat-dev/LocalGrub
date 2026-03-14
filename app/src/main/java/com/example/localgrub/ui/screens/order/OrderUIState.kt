package com.example.localgrub.ui.screens.order

import com.example.localgrub.data.model.firebase.FetchedOrder
import com.example.localgrub.domain.mapper.firebase.WriteReqDomainFailure

sealed interface OrderUIState {
    object Idle: OrderUIState
    object Loading: OrderUIState
    data class Success(val order: FetchedOrder): OrderUIState
    data class Failure(val failure: WriteReqDomainFailure): OrderUIState
    data class ValidationError(val message: String): OrderUIState
    object NoInternet: OrderUIState
}