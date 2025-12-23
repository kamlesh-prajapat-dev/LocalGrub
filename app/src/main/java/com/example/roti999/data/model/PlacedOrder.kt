package com.example.roti999.data.model

import com.google.firebase.Timestamp

data class PlacedOrder(
    val userId: String = "",
    val userName: String = "",
    val userAddress: String = "",
    val userPhoneNumber: String = "",
    val items: List<SelectedDish> = emptyList(),
    val totalPrice: Double = 0.0,
    val placeAt: Timestamp = Timestamp.now(),
    val status: String = "Placed",
    val token: String = ""
)
