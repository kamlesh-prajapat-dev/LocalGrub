package com.example.roti999.ui.screens.order

import com.example.roti999.data.model.FetchedOrder

sealed interface OrderUIState {
    object Idle: OrderUIState
    object Loading: OrderUIState
    data class Success(val order: FetchedOrder): OrderUIState
    data class Error(val e: Exception): OrderUIState
    data class ValidationError(val message: String): OrderUIState
}