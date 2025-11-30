package com.example.roti999.data.model

import com.example.roti999.domain.model.FoodItem
import com.google.firebase.Timestamp
import java.time.LocalDate

data class OrderPlaced(
    val userId: String = "",
    val userName: String = "",
    val userAddress: String = "",
    val userPhoneNumber: String = "",
    val items: List<SelectedDishItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val placeAt: Timestamp = Timestamp.now(),
    val status: String = "Placed"
)
