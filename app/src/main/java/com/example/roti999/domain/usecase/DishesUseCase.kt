package com.example.roti999.domain.usecase

import com.example.roti999.data.model.DishItem
import com.example.roti999.domain.model.DishResult
import com.example.roti999.domain.model.FoodItem
import com.example.roti999.domain.repository.DishesRepository
import com.example.roti999.ui.screens.home.HomeUIState
import javax.inject.Inject

class DishesUseCase @Inject constructor(
    private val dishesRepository: DishesRepository
) {
    suspend fun getDishes(): HomeUIState {
        return when(val dishResult = dishesRepository.getDishes()) {
            is DishResult.Success -> {
                val dishes = dishResult.dishes
                if (dishes.isNotEmpty()) {
                    val foodItems = converter(dishes)
                    HomeUIState.Success(foodItems)
                } else {
                    HomeUIState.Success(emptyList())
                }
            }

            is DishResult.Error -> {
                HomeUIState.Error(dishResult.e)
            }
        }
    }

    private fun converter(dishItem: List<DishItem?>): List<FoodItem> {
        return dishItem.map {
            FoodItem(
                id = it?.id ?: "",
                name = it?.name ?: "",
                description = it?.description ?: "",
                price = it?.price ?: 0,
                imageUrl = it?.thumbnail ?: "",
                quantity = 0,
                isSelected = false
            )
        }
    }
}