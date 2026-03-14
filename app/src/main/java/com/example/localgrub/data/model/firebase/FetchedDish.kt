package com.example.localgrub.data.model.firebase

data class FetchedDish(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Int = 0,
    val thumbnail: String = "",
    val available: Boolean = false
)
