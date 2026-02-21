package com.example.localgrub.domain.usecase

import android.util.Log
import com.example.localgrub.data.model.FetchedOrder
import com.example.localgrub.data.model.NotificationRequest
import com.example.localgrub.data.model.PlacedOrder
import com.example.localgrub.domain.mapper.firebase.toGetReqDomainFailure
import com.example.localgrub.domain.mapper.firebase.toWriteReqDomainFailure
import com.example.localgrub.domain.model.result.NotificationResult
import com.example.localgrub.domain.model.result.OrderResult
import com.example.localgrub.domain.model.result.TokenResult
import com.example.localgrub.domain.repository.NotificationRepository
import com.example.localgrub.domain.repository.OrderRepository
import com.example.localgrub.domain.repository.TokenRepository
import com.example.localgrub.ui.screens.eachorderstatus.EachOrderUIState
import com.example.localgrub.ui.screens.history.HistoryUIState
import com.example.localgrub.ui.screens.order.OrderUIState
import com.example.localgrub.util.UnknownException
import com.example.localgrub.workerscheduler.SenderNotificationWorkerScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
    private val tokenRepository: TokenRepository,
    private val notificationRepository: NotificationRepository,
    private val senderNotificationWorkerScheduler: SenderNotificationWorkerScheduler
) {
    suspend fun placeOrder(orderPlaced: PlacedOrder): OrderUIState {
        return when (val result = orderRepository.placeOrder(orderPlaced)) {
            is OrderResult.OrderCreateSuccess -> {
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

            is OrderResult.Failure -> {
                OrderUIState.Failure(result.e.toWriteReqDomainFailure(orderPlaced))
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

            is OrderResult.Failure -> {
                EachOrderUIState.CancelOrderFailure(failure = result.e.toWriteReqDomainFailure(orderId))
            }

            else -> EachOrderUIState.Idle
        }
    }

    private suspend fun notifyOwner(docId: String): NotificationResult {
        return when (val result = tokenRepository.getToken(docId)) {
            is TokenResult.TokenGetSuccess -> {
                val token = result.tokenData.token
                if (token.isBlank())
                    return NotificationResult.Error(Exception("Invalid token"))
                val response = notificationRepository.sendNotification(
                    NotificationRequest(
                        token = token,
                        title = "New Order Received",
                        body = "You have a new order! Order ID: $docId",
                        orderId = docId
                    )
                )

                NotificationResult.Success(true)
            }

            is TokenResult.Failure -> {
                val exception = result.exception
                senderNotificationWorkerScheduler.retryNotification(orderId = docId)
                return NotificationResult.Error(result.exception)
            }

            else -> NotificationResult.Error(UnknownException("Invalid token"))
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

                    is OrderResult.Failure -> {
                        HistoryUIState.Failure(result.e.toGetReqDomainFailure("Orders data"))
                    }

                    else -> HistoryUIState.Idle
                }
            }
            .catch {
                emit(HistoryUIState.Failure(it.toGetReqDomainFailure("Orders data")))
            }
    }

    fun observeOrderById(orderId: String): Flow<EachOrderUIState> {
        return orderRepository.observeOrderById(orderId)
            .map { result ->
                when(result) {
                    is OrderResult.OrderGetSuccessByOrderId -> {
                        EachOrderUIState.OrderGetSuccess(result.order)
                    }
                    is OrderResult.Failure -> {
                        EachOrderUIState.OrderGetFailure(result.e.toGetReqDomainFailure(orderId))
                    }
                    else -> EachOrderUIState.Idle
                }
            }.catch {
                emit(EachOrderUIState.OrderGetFailure(it.toGetReqDomainFailure(orderId)))
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
            previousStatus = orderPlaced.previousStatus
        )
    }
}