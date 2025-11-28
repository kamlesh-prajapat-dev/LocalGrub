package com.example.roti999.data.model

data class DishItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Int = 0,
    val thumbnail: String = "",
    val addOns: List<AddOn> = emptyList()
)
