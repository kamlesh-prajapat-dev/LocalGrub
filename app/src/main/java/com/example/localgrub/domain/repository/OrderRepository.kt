package com.example.localgrub.domain.repository

import com.example.localgrub.data.model.firebase.PlacedOrder
import com.example.localgrub.domain.model.result.OrderResult
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    suspend fun placeOrder(orderPlaced: PlacedOrder): OrderResult
    fun observeOrders(userId: String): Flow<OrderResult>
    suspend fun cancelOrder(orderId: String, cancelStatus: String, status: String): OrderResult
    fun observeOrderById(orderId: String): Flow<OrderResult>
}