package com.example.roti999.domain.usecase

import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.roti999.data.model.NotificationRequest
import com.example.roti999.data.model.FetchedOrder
import com.example.roti999.data.model.PlacedOrder
import com.example.roti999.domain.model.NotificationResult
import com.example.roti999.domain.model.OrderResult
import com.example.roti999.domain.repository.NotificationRepository
import com.example.roti999.domain.repository.OrderRepository
import com.example.roti999.domain.repository.OwnerRepository
import com.example.roti999.ui.screens.history.HistoryUIState
import com.example.roti999.ui.screens.order.OrderUIState
import com.example.roti999.worker.SenderNotificationWorker
import com.example.roti999.workerscheduler.SenderNotificationWorkerScheduler
import javax.inject.Inject

class OrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
    private val ownerRepository: OwnerRepository,
    private val notificationRepository: NotificationRepository,
    private val senderNotificationWorkerScheduler: SenderNotificationWorkerScheduler
){
    suspend fun placeOrder(orderPlaced: PlacedOrder): OrderUIState {
        return when(val result = orderRepository.placeOrder(orderPlaced)) {
            is OrderResult.Success -> {
                when(val notifyResult = notifyOwner(result.docId)) {
                    is NotificationResult.Success -> {
                        Log.d("OrderUseCase", "Notification sent successfully")
                    }
                    is NotificationResult.Error -> {
                        Log.e("OrderUseCase", "Error sending notification", notifyResult.e)
                    }
                }

                val order = converter(orderPlaced, result.docId)
                OrderUIState.Success(order)
            }
            is OrderResult.Error -> {
                OrderUIState.Error(result.e)
            }
            else -> OrderUIState.Idle
        }
    }

    private suspend fun notifyOwner(docId: String): NotificationResult {
        return when(val result = ownerRepository.getOwnerFcmToken()) {
            is com.example.roti999.domain.model.OwnerResult.Success -> {
                val token = result.token
                if (token.isBlank()) return NotificationResult.Error(Exception("Invalid token"))
                val notifyResult = notificationRepository.sendNotification(
                    NotificationRequest(
                        token = token,
                        title = "New Order Received",
                        body = "You have a new order! Order ID: $docId",
                        orderId = docId
                    )
                )

                NotificationResult.Success(true)
            }

            is com.example.roti999.domain.model.OwnerResult.Error -> {
                senderNotificationWorkerScheduler.retryNotification(orderId = docId)
                return NotificationResult.Error(result.e)
            }
        }
    }

    suspend fun getOrders(userId: String): HistoryUIState {
        return when(val result = orderRepository.getOrders(userId)) {
            is OrderResult.OrdersSuccess -> {
                val orders = result.orders.sortedByDescending { it.placeAt }
                HistoryUIState.Success(orders)
            }
            is OrderResult.Error -> {
                HistoryUIState.Error(result.e)
            }
            else -> HistoryUIState.Idle
        }
    }

    private fun converter(orderPlaced: PlacedOrder, docId: String): FetchedOrder {
        return FetchedOrder(
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
    }
}