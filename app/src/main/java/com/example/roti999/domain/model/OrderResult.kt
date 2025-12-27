package com.example.roti999.domain.model

import com.example.roti999.data.model.FetchedOrder
import com.example.roti999.data.model.PlacedOrder

sealed interface OrderResult {
    data class Success(val orderPlaced: PlacedOrder, val docId: String): OrderResult
    data class Error(val e: Exception): OrderResult
    data class OrdersSuccess(val orders: List<FetchedOrder>): OrderResult
    data class OrderCancelSuccess(val isSuccess: Boolean): OrderResult
}