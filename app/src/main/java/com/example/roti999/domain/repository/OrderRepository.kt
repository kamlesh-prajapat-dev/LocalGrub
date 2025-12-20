package com.example.roti999.domain.repository

import com.example.roti999.data.model.OrderPlaced
import com.example.roti999.domain.model.OrderResult
import com.example.roti999.ui.screens.history.HistoryUIState
import com.example.roti999.ui.screens.order.OrderUIState

interface OrderRepository {

    suspend fun placeOrder(orderPlaced: OrderPlaced): OrderResult

    suspend fun getOrders(userId: String): OrderResult
}