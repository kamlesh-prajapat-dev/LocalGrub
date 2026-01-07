package com.example.roti999.domain.repository

import com.example.roti999.data.model.PlacedOrder
import com.example.roti999.domain.model.OrderResult
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    suspend fun placeOrder(orderPlaced: PlacedOrder): OrderResult
    fun observeOrders(userId: String): Flow<OrderResult>
    suspend fun cancelOrder(orderId: String, cancelStatus: String, status: String): OrderResult
    fun observeOrderById(orderId: String): Flow<OrderResult>
}