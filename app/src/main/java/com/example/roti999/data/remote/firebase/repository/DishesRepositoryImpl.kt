package com.example.roti999.data.remote.firebase.repository

import com.example.roti999.data.model.DishItem
import com.example.roti999.domain.model.DishResult
import com.example.roti999.domain.repository.DishesRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DishesRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
): DishesRepository {
    override suspend fun getDishes(): DishResult {
        return try {
            val snapshot = firestore.collection("dishes")
                .whereEqualTo("isAvailable", true)
                .get()
                .await()

            val documents = snapshot.documents

            if (documents.isNotEmpty()) {
                val dishes = snapshot.documents.map {
                    it.toObject(DishItem::class.java)
                        ?.copy(id = it.id)
                }
                DishResult.Success(dishes)
            } else {
                DishResult.Success(emptyList())
            }
        } catch (e: Exception) {
            DishResult.Error(e)
        }
    }
}