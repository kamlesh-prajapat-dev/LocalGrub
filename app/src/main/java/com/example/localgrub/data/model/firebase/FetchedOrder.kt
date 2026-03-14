package com.example.localgrub.data.model.firebase

import com.example.localgrub.util.OrderStatus

data class FetchedOrder(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userAddress: String = "",
    val userPhoneNumber: String = "",
    val items: List<SelectedDish> = emptyList(),
    val totalPrice: Double = 0.0,
    val placeAt: Long = 0L,
    val status: String = OrderStatus.PLACED,
    val previousStatus: String = OrderStatus.PLACED,
    val name: String = "",
)
