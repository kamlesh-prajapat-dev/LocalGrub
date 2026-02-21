package com.example.localgrub.domain.repository

import com.example.localgrub.domain.model.result.DishResult
import kotlinx.coroutines.flow.Flow


interface DishesRepository {

    fun observeDishes(): Flow<DishResult>
}