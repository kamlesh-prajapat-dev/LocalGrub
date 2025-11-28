package com.example.roti999.domain.repository

import com.example.roti999.data.model.Order

interface OrderRepository {

    suspend fun placeOrder(order: Order, onResult: (Boolean) -> Unit)
}