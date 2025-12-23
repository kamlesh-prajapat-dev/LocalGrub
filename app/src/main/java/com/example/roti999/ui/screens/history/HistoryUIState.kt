package com.example.roti999.ui.screens.history

import com.example.roti999.data.model.FetchedOrder

sealed interface HistoryUIState {
    object Idle : HistoryUIState
    object Loading : HistoryUIState
    data class Success(val orders: List<FetchedOrder>) : HistoryUIState
    data class Error(val e: Exception) : HistoryUIState
    object NoInternet: HistoryUIState
    object NavigateToCreateProfile: HistoryUIState
}