package com.example.roti999.data.model

data class PlacedOrder(
    val userId: String = "",
    val userName: String = "",
    val userAddress: String = "",
    val userPhoneNumber: String = "",
    val items: List<SelectedDish> = emptyList(),
    val totalPrice: Double = 0.0,
    val placeAt: Long = 0L,
    val status: String = "Placed",
    val previousStatus: String = "Placed",
    val token: String = ""
)
