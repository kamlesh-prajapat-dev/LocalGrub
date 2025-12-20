package com.example.roti999.ui.screens.home

import com.example.roti999.data.model.User
import com.example.roti999.domain.model.FoodItem

sealed class HomeUIState {
    object Idle : HomeUIState()
    object Loading : HomeUIState()
    data class SuccessUser(val user: User?): HomeUIState()
    data class Success(val dishes: List<FoodItem>) : HomeUIState()
    data class Error(val e: Exception) : HomeUIState()
    object NoInternet: HomeUIState()
    object NavigateToLogin: HomeUIState()
}