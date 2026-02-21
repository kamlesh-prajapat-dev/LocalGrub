package com.example.localgrub.domain.model.result

import com.example.localgrub.data.model.FetchedOrder
import com.example.localgrub.data.model.PlacedOrder

sealed interface OrderResult {
    data class OrderCreateSuccess(val orderPlaced: PlacedOrder, val docId: String): OrderResult
    data class Failure(val e: Exception): OrderResult
    data class OrdersGetSuccess(val orders: List<FetchedOrder>): OrderResult
    data class OrderCancelSuccess(val isSuccess: Boolean): OrderResult
    data class OrderGetSuccessByOrderId(val order: FetchedOrder): OrderResult
}