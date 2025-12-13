package com.example.roti999.data.repository

import com.example.roti999.data.model.Order
import com.example.roti999.data.model.OrderPlaced
import com.example.roti999.domain.model.OrderHistoryResult
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
        orderPlaced: OrderPlaced,
        onResult: (String?) -> Unit
    ) {
        try {
            var docId: String? = null
            firestore.collection("orders").add(orderPlaced)
                .addOnSuccessListener { documentReference ->
                    docId = documentReference.id
                }
                .await()
            onResult(docId)
        } catch (e: Exception) {
            onResult(null)
        }
    }

    override suspend fun getOrders(userId: String, onResult: (OrderHistoryResult) -> Unit) {
        try {
            val snapshot = firestore.collection("orders")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val orders = snapshot.documents.map {
                it.toObject(Order::class.java)
                    ?.copy(id = it.id) ?: Order()
            }.sortedByDescending { it.placeAt }
            onResult(OrderHistoryResult.Success(orders))
        } catch (e: Exception) {
            onResult(OrderHistoryResult.Error(e.message ?: "Unknown error occurred"))
        }
    }
}