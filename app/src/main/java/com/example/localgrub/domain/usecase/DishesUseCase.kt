package com.example.localgrub.domain.usecase

import com.example.localgrub.data.model.FetchedDish
import com.example.localgrub.domain.mapper.firebase.toGetReqDomainFailure
import com.example.localgrub.domain.model.result.DishResult
import com.example.localgrub.data.model.FoodItem
import com.example.localgrub.domain.repository.DishesRepository
import com.example.localgrub.ui.screens.home.HomeUIState
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
                        HomeUIState.DishGetSuccess(converter(dishResult.dishes))
                    }

                    is DishResult.Error -> {
                        HomeUIState.FirebaseGetFailure(dishResult.e.toGetReqDomainFailure("Menu Data"))
                    }
                }
            }
            .catch { exception ->
                emit(HomeUIState.FirebaseGetFailure(exception.toGetReqDomainFailure("Menu Data")))
            }
    }

    private fun converter(dishItem: List<FetchedDish>): List<FoodItem> {
        return if (dishItem.isEmpty()) emptyList()
        else dishItem.map {
            FoodItem(
                id = it.id,
                name = it.name,
                description = it.description,
                price = it.price,
                imageUrl = it.thumbnail,
                quantity = 0,
                available = it.available,
                isSelected = false
            )
        }
    }
}