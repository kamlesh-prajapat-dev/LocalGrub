package com.example.roti999.domain.repository

import com.example.roti999.data.model.OrderPlaced
import com.example.roti999.domain.model.OrderHistoryResult

interface OrderRepository {

    suspend fun placeOrder(orderPlaced: OrderPlaced, onResult: (Boolean) -> Unit)

    suspend fun getOrders(userId: String, onResult: (OrderHistoryResult) -> Unit)
}