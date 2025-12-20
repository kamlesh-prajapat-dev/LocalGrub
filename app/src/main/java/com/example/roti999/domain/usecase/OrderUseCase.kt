package com.example.roti999.domain.usecase

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.roti999.data.model.NotificationRequest
import com.example.roti999.data.model.Order
import com.example.roti999.data.model.OrderPlaced
import com.example.roti999.domain.model.OrderResult
import com.example.roti999.domain.repository.NotificationRepository
import com.example.roti999.domain.repository.OrderRepository
import com.example.roti999.domain.repository.OwnerRepository
import com.example.roti999.ui.screens.history.HistoryUIState
import com.example.roti999.ui.screens.order.OrderUIState
import com.example.roti999.worker.SenderNotificationWorker
import javax.inject.Inject
import javax.inject.Singleton

class OrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
    private val ownerRepository: OwnerRepository,
    private val notificationRepository: NotificationRepository,
    private val workManager: WorkManager
){
    suspend fun placeOrder(orderPlaced: OrderPlaced): OrderUIState {
        return when(val result = orderRepository.placeOrder(orderPlaced)) {
            is OrderResult.Success -> {
                when(val result2 = ownerRepository.getOwnerFcmToken()) {
                    is com.example.roti999.domain.model.OwnerResult.Success -> {
                        val token = result2.token
                        if (token.isBlank()) return OrderUIState.Error(Exception("Invalid token"))
                        notificationRepository.sendNotification(
                            NotificationRequest(
                                token = token,
                                title = "New Order Received",
                                body = "You have a new order! Order ID: ${result.docId}",
                                orderId = result.docId
                            )
                        )
                    }

                    is com.example.roti999.domain.model.OwnerResult.Error -> {
                        val workRequest = OneTimeWorkRequestBuilder<SenderNotificationWorker>()
                            .setInputData(
                                workDataOf("ORDER_ID" to result.docId)
                            )
                            .build()

                        workManager.enqueue(workRequest)
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

    private fun converter(orderPlaced: OrderPlaced, docId: String): Order {
        return Order(
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