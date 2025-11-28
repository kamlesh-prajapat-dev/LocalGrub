package com.example.roti999.data.model

import java.time.LocalDate

data class Order(
    val userId: String = "",
    val userName: String = "",
    val userAddress: String = "",
    val userPhoneNumber: String = "",
    val items: List<String> = emptyList(),
    val totalPrice: Double = 0.0,
    val orderDate: LocalDate = LocalDate.now(),
    val status: String = "Pending"
)
