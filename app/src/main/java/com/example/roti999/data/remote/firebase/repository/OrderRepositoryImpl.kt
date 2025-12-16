package com.example.roti999.data.remote.firebase.repository

import com.example.roti999.data.dto.NotificationRequest
import com.example.roti999.data.dto.Order
import com.example.roti999.data.dto.OrderPlaced
import com.example.roti999.domain.repository.NotificationRepository
import com.example.roti999.ui.screens.history.HistoryUIState
import com.example.roti999.domain.repository.OrderRepository
import com.example.roti999.ui.screens.order.OrderUIState
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
        onResult: (OrderUIState) -> Unit
    ) {
        try {
            var docId = ""
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
                        orderId = docId
                    )
                )
            }

            onResult(OrderUIState.Success(
                Order(
                    id = docId,
                    userId = orderPlaced.userId,
                    userName = orderPlaced.userName,
                    userAddress = orderPlaced.userAddress,
                    userPhoneNumber = orderPlaced.userPhoneNumber,
                    items = orderPlaced.items,
                    totalPrice = orderPlaced.totalPrice,
                    placeAt = orderPlaced.placeAt,
                    status = orderPlaced.status,
                    token = orderPlaced.token
                )
            ))
        } catch (e: Exception) {
            onResult(OrderUIState.Error(e))
        }
    }

    override suspend fun getOrders(userId: String, onResult: (HistoryUIState) -> Unit) {
        try {
            val snapshot = firestore.collection("orders")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val orders = snapshot.documents.map {
                it.toObject(Order::class.java)
                    ?.copy(id = it.id) ?: Order()
            }.sortedByDescending { it.placeAt }
            onResult(HistoryUIState.Success(orders))
        } catch (e: Exception) {
            onResult(HistoryUIState.Error(e.message ?: "Unknown error occurred"))
        }
    }
}