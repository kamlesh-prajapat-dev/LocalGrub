package com.example.roti999.domain.repository

import com.example.roti999.ui.screens.home.HomeUIState


interface DishesRepository {

    suspend fun getDishes(): HomeUIState
}