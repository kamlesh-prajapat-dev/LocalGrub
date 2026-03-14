package com.example.localgrub.domain.model.result

import com.example.localgrub.data.model.firebase.FetchedDish

sealed interface DishResult {
    data class Success(val dishes: List<FetchedDish>) : DishResult
    data class Error(val e: Exception) : DishResult
}