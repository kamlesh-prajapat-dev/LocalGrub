package com.example.roti999.domain.usecase

import android.util.Log
import com.example.roti999.data.model.FetchedOrder
import com.example.roti999.data.model.NotificationRequest
import com.example.roti999.data.model.PlacedOrder
import com.example.roti999.domain.model.NotificationResult
import com.example.roti999.domain.model.OrderResult
import com.example.roti999.domain.repository.NotificationRepository
import com.example.roti999.domain.repository.OrderRepository
import com.example.roti999.domain.repository.OwnerRepository
import com.example.roti999.ui.screens.eachorderstatus.EachOrderUIState
import com.example.roti999.ui.screens.history.HistoryUIState
import com.example.roti999.ui.screens.order.OrderUIState
import com.example.roti999.workerscheduler.SenderNotificationWorkerScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
    private val ownerRepository: OwnerRepository,
    private val notificationRepository: NotificationRepository,
    private val senderNotificationWorkerScheduler: SenderNotificationWorkerScheduler
) {
    suspend fun placeOrder(orderPlaced: PlacedOrder): OrderUIState {
        return when (val result = orderRepository.placeOrder(orderPlaced)) {
            is OrderResult.Success -> {
                when (val notifyResult = notifyOwner(result.docId)) {
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

    suspend fun cancelOrder(orderId: String, cancelStatus: String, status: String): EachOrderUIState {
        return when (val result = orderRepository.cancelOrder(orderId, cancelStatus, status)) {
            is OrderResult.OrderCancelSuccess -> {
                when (val notifyResult = notifyOwner(orderId)) {
                    is NotificationResult.Success -> {
                        Log.d("OrderUseCase", "Notification sent successfully")
                    }

                    is NotificationResult.Error -> {
                        Log.e("OrderUseCase", "Error sending notification", notifyResult.e)
                    }
                }
                EachOrderUIState.Success(result.isSuccess)
            }

            is OrderResult.Error -> {
                EachOrderUIState.Failure(exception = result.e)
            }

            else -> EachOrderUIState.Idle
        }
    }

    private suspend fun notifyOwner(docId: String): NotificationResult {
        return when (val result = ownerRepository.getOwnerFcmToken()) {
            is com.example.roti999.domain.model.OwnerResult.Success -> {
                val token = result.token
                if (token.isBlank()) return NotificationResult.Error(Exception("Invalid token"))
                notificationRepository.sendNotification(
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

    fun observeOrders(userId: String): Flow<HistoryUIState> {
        return orderRepository.observeOrders(userId)
            .map { result ->
                when (result) {
                    is OrderResult.OrdersGetSuccess -> {
                        val sortedOrders =
                            result.orders.sortedByDescending { it.placeAt }
                        HistoryUIState.Success(sortedOrders)
                    }

                    is OrderResult.Error -> {
                        HistoryUIState.Error(result.e)
                    }

                    else -> HistoryUIState.Idle
                }
            }
            .catch {
                emit(HistoryUIState.Error(it as Exception))
            }
    }

    fun observeOrderById(orderId: String): Flow<EachOrderUIState> {
        return orderRepository.observeOrderById(orderId)
            .map { result ->
                when(result) {
                    is OrderResult.OrderGetSuccessByOrderId -> {
                        EachOrderUIState.OrderGetSuccess(result.order)
                    }
                    is OrderResult.Error -> {
                        EachOrderUIState.Failure(result.e)
                    }
                    else -> EachOrderUIState.Idle
                }
            }.catch {
                emit(EachOrderUIState.Failure(it as Exception))
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
            previousStatus = orderPlaced.previousStatus,
            token = orderPlaced.token
        )
    }
}