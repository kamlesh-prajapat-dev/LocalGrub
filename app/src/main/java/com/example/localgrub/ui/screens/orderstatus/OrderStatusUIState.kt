package com.example.localgrub.ui.screens.orderstatus

import com.example.localgrub.data.model.firebase.FetchedOrder
import com.example.localgrub.domain.mapper.firebase.GetReqDomainFailure
import com.example.localgrub.domain.mapper.firebase.WriteReqDomainFailure

sealed interface OrderStatusUIState {
    object Idle: OrderStatusUIState
    object Loading: OrderStatusUIState
    data class Success(val isSuccess: Boolean): OrderStatusUIState
    data class CancelOrderFailure(val failure: WriteReqDomainFailure): OrderStatusUIState
    data class OrderGetFailure(val failure: GetReqDomainFailure): OrderStatusUIState
    data class OrderGetSuccess(val order: FetchedOrder): OrderStatusUIState
    object NoInternet: OrderStatusUIState
}