package com.example.roti999.domain.repository

import com.example.roti999.domain.model.DishesResult

interface DishesRepository {

    suspend fun getDishes(): DishesResult
}