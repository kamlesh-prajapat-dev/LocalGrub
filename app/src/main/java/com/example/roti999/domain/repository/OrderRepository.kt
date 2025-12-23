package com.example.roti999.domain.repository

import com.example.roti999.data.model.PlacedOrder
import com.example.roti999.domain.model.OrderResult

interface OrderRepository {

    suspend fun placeOrder(orderPlaced: PlacedOrder): OrderResult

    suspend fun getOrders(userId: String): OrderResult
}