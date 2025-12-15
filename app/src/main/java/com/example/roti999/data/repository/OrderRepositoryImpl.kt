package com.example.roti999.data.repository

import com.example.roti999.data.model.NotificationRequest
import com.example.roti999.data.model.Order
import com.example.roti999.data.model.OrderPlaced
import com.example.roti999.domain.repository.NotificationRepository
import com.example.roti999.ui.screens.history.OrderHistoryResult
import com.example.roti999.domain.repository.OrderRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val notificationRepository: NotificationRepository
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

            CoroutineScope(Dispatchers.IO).launch {
                val snapshot = firestore.collection("owners").get().await()
                val token = snapshot.documents[0].getString("token") ?: ""

                notificationRepository.sendNotification(
                    NotificationRequest(
                        token = token,
                        title = "New Order Placed",
                        body = "Your order has been placed successfully.",
                        orderId = docId ?: ""
                    )
                )
            }


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