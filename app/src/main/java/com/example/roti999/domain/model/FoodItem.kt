package com.example.roti999.domain.model

data class FoodItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Int = 0,
    val imageUrl: String = "", // Optional image URL
    val addOns: List<AddOn> = emptyList(),
    var quantity: Int = 1,
    val isSelected: Boolean = false
)
