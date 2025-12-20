package com.example.roti999.domain.model

import com.example.roti999.data.model.DishItem

sealed interface DishResult {
    data class Success(val dishes: List<DishItem?>) : DishResult
    data class Error(val e: Exception) : DishResult
}