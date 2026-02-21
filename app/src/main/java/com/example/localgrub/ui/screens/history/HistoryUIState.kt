package com.example.localgrub.ui.screens.history

import com.example.localgrub.data.model.FetchedOrder
import com.example.localgrub.domain.mapper.firebase.GetReqDomainFailure

sealed interface HistoryUIState {
    object Idle : HistoryUIState
    object Loading : HistoryUIState
    data class Success(val orders: List<FetchedOrder>) : HistoryUIState
    data class Failure(val failure: GetReqDomainFailure) : HistoryUIState
    object NoInternet: HistoryUIState
}