package com.example.roti999.ui.screens.history

import com.example.roti999.data.dto.Order

sealed interface HistoryUIState {
    object Idle : HistoryUIState
    object Loading : HistoryUIState
    data class Success(val orders: List<Order>) : HistoryUIState
    data class Error(val message: String) : HistoryUIState
    object NoInternet: HistoryUIState
}