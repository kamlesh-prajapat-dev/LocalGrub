package com.example.roti999.data.model

data class FetchedOrder(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userAddress: String = "",
    val userPhoneNumber: String = "",
    val items: List<SelectedDish> = emptyList(),
    val totalPrice: Double = 0.0,
    val placeAt: Long = 0L,
    val status: String = "Placed",
    val previousStatus: String = "Placed",
    val name: String = "",
    val token: String = ""
)
