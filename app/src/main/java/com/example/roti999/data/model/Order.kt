package com.example.roti999.data.model

import com.google.firebase.Timestamp

data class Order(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userAddress: String = "",
    val userPhoneNumber: String = "",
    val items: List<SelectedDishItem> = emptyList(), // quantity, dishItem
    val totalPrice: Double = 0.0,
    val placeAt: Timestamp = Timestamp.now(),
    val status: String = "Placed"
)
