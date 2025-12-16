package com.example.roti999.domain.repository

import com.example.roti999.data.dto.OrderPlaced
import com.example.roti999.ui.screens.history.HistoryUIState
import com.example.roti999.ui.screens.order.OrderUIState

interface OrderRepository {

    suspend fun placeOrder(orderPlaced: OrderPlaced, onResult: (OrderUIState) -> Unit)

    suspend fun getOrders(userId: String, onResult: (HistoryUIState) -> Unit)
}