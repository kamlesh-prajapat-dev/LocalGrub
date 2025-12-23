package com.example.roti999.domain.model

import com.example.roti999.data.model.FetchedDish

sealed interface DishResult {
    data class Success(val dishes: List<FetchedDish?>) : DishResult
    data class Error(val e: Exception) : DishResult
}