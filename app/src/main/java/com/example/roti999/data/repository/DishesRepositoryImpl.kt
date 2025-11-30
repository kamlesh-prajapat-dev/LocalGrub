package com.example.roti999.data.repository

import com.example.roti999.data.model.DishItem
import com.example.roti999.domain.model.DishesResult
import com.example.roti999.domain.model.FoodItem
import com.example.roti999.domain.repository.DishesRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DishesRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
): DishesRepository {
    override suspend fun getDishes(): DishesResult {
        return try {
            val snapshot = firestore.collection("dishes")
                .whereEqualTo("isAvailable", true)
                .get()
                .await()
            val dishes = snapshot.documents.map {
                it.toObject(DishItem::class.java)
                    ?.copy(id = it.id) ?: DishItem()
            }
            DishesResult.Success(
                dishes = dishes.map {
                    FoodItem(
                        id = it.id,
                        name = it.name,
                        description = it.description,
                        price = it.price,
                        imageUrl = it.thumbnail
                    )
                }
            )
        } catch (e: Exception) {
            DishesResult.Error(e.message ?: "Unknown error occurred")
        }
    }
}