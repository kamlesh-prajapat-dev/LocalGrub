package com.example.localgrub.data.model.firebase

data class FoodItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Int = 0,
    val imageUrl: String = "",
    var quantity: Int = 1,
    val available: Boolean = false,
    val isSelected: Boolean = false
)