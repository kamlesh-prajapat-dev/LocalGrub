package com.example.roti999.domain.model

import com.example.roti999.data.model.Order
import com.example.roti999.data.model.OrderPlaced

sealed interface OrderResult {
    data class Success(val orderPlaced: OrderPlaced, val docId: String): OrderResult
    data class Error(val e: Exception): OrderResult
    data class OrdersSuccess(val orders: List<Order>): OrderResult
}