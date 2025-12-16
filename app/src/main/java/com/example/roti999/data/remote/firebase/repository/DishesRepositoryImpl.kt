package com.example.roti999.data.remote.firebase.repository

import com.example.roti999.data.dto.DishItem
import com.example.roti999.domain.model.FoodItem
import com.example.roti999.domain.repository.DishesRepository
import com.example.roti999.ui.screens.home.HomeUIState
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DishesRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
): DishesRepository {
    override suspend fun getDishes(): HomeUIState {
        return try {
            val snapshot = firestore.collection("dishes")
                .whereEqualTo("isAvailable", true)
                .get()
                .await()
            val dishes = snapshot.documents.map {
                it.toObject(DishItem::class.java)
                    ?.copy(id = it.id)
            }
            HomeUIState.Success(
                dishes = dishes.map {
                    FoodItem(
                        id = it?.id ?: "",
                        name = it?.name ?: "",
                        description = it?.description ?: "",
                        price = it?.price ?: 0,
                        imageUrl = it?.thumbnail ?: ""
                    )
                }
            )
        } catch (e: Exception) {
            HomeUIState.Error(e.message ?: "Unknown error occurred")
        }
    }
}