package com.example.roti999.data.repository

import com.example.roti999.data.model.Order
import com.example.roti999.domain.repository.OrderRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
): OrderRepository {
    override suspend fun placeOrder(
        order: Order,
        onResult: (Boolean) -> Unit
    ) {
        try {
            firestore.collection("orders").add(order).await()
            onResult(true)
        } catch (e: Exception) {
            onResult(false)
        }
    }
}