package com.example.roti999.data.remote.firebase.repository

import com.example.roti999.data.model.Order
import com.example.roti999.data.model.OrderPlaced
import com.example.roti999.domain.model.OrderResult
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
        orderPlaced: OrderPlaced
    ): OrderResult {
        return try {
            var docId = ""
            firestore.collection("orders").add(orderPlaced)
                .addOnSuccessListener { documentReference ->
                    docId = documentReference.id
                }
                .await()

            OrderResult.Success(orderPlaced, docId)
        } catch (e: Exception) {
            OrderResult.Error(e)
        }
    }

    override suspend fun getOrders(userId: String): OrderResult {
        return try {
            val snapshot = firestore.collection("orders")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val orders = snapshot.documents.map {
                it.toObject(Order::class.java)
                    ?.copy(id = it.id) ?: Order()
            }
            OrderResult.OrdersSuccess(orders)
        } catch (e: Exception) {
            OrderResult.Error(e)
        }
    }
}