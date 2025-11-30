package com.example.roti999.domain.model

import com.example.roti999.data.model.Order

sealed class OrderHistoryResult {
    object Idle : OrderHistoryResult()
    object Loading : OrderHistoryResult()
    data class Success(val orders: List<Order>) : OrderHistoryResult()
    data class Error(val message: String) : OrderHistoryResult()
}