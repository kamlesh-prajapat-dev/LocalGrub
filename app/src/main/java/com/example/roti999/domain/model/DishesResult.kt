package com.example.roti999.domain.model

sealed class DishesResult {
    object Idle : DishesResult()
    object Loading : DishesResult()
    data class Success(val dishes: List<FoodItem>) : DishesResult()
    data class Error(val message: String) : DishesResult()
}