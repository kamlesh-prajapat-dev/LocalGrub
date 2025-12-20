package com.example.roti999.ui.screens.history

import com.example.roti999.data.model.Order

sealed interface HistoryUIState {
    object Idle : HistoryUIState
    object Loading : HistoryUIState
    data class Success(val orders: List<Order>) : HistoryUIState
    data class Error(val e: Exception) : HistoryUIState
    object NoInternet: HistoryUIState
    object NavigateToCreateProfile: HistoryUIState
}