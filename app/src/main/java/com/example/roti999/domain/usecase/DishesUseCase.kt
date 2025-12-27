package com.example.roti999.domain.usecase

import com.example.roti999.data.model.FetchedDish
import com.example.roti999.domain.model.DishResult
import com.example.roti999.domain.model.FoodItem
import com.example.roti999.domain.repository.DishesRepository
import com.example.roti999.ui.screens.home.HomeUIState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DishesUseCase @Inject constructor(
    private val dishesRepository: DishesRepository
) {
    fun getDishes(): Flow<HomeUIState> {
        return dishesRepository.observeDishes()
            .map { dishResult ->
                when (dishResult) {
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
            .catch { exception ->
                emit(HomeUIState.Error(exception as Exception))
            }
    }

    private fun converter(dishItem: List<FetchedDish?>): List<FoodItem> {
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