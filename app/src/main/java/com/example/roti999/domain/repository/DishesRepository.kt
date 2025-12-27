package com.example.roti999.domain.repository

import com.example.roti999.domain.model.DishResult
import kotlinx.coroutines.flow.Flow


interface DishesRepository {

    fun observeDishes(): Flow<DishResult>
}