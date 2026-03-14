package com.example.localgrub.domain.usecase

import android.util.Log
import com.example.localgrub.data.model.api.request.NotificationRequest
import com.example.localgrub.data.model.firebase.FetchedOrder
import com.example.localgrub.data.model.firebase.PlacedOrder
import com.example.localgrub.domain.mapper.firebase.toGetReqDomainFailure
import com.example.localgrub.domain.mapper.firebase.toWriteReqDomainFailure
import com.example.localgrub.domain.model.result.NotificationResult
import com.example.localgrub.domain.model.result.OrderResult
import com.example.localgrub.domain.repository.NotificationRepository
import com.example.localgrub.domain.repository.OrderRepository
import com.example.localgrub.ui.screens.history.HistoryUIState
import com.example.localgrub.ui.screens.order.OrderUIState
import com.example.localgrub.ui.screens.orderstatus.OrderStatusUIState
import com.example.localgrub.workerscheduler.SenderNotificationWorkerScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
    private val notificationRepository: NotificationRepository,
    private val senderNotificationWorkerScheduler: SenderNotificationWorkerScheduler
) {
    suspend fun placeOrder(orderPlaced: PlacedOrder): OrderUIState {
        return when (val result = orderRepository.placeOrder(orderPlaced)) {
            is OrderResult.OrderCreateSuccess -> {
                val orderId = result.docId
                when (val notifyResult = notifyOwner(
                    userId = orderPlaced.userId,
                    orderId = orderId,
                    status = orderPlaced.status,
                    userName = orderPlaced.userName
                )) {
                    is NotificationResult.Success -> {
                        Log.i("OrderUseCase", notifyResult.message)
                    }

                    is NotificationResult.Failure -> {
                        Log.e(
                            "OrderUseCase",
                            notifyResult.exception.message,
                            notifyResult.exception
                        )
                        senderNotificationWorkerScheduler.retryNotification(
                            userId = orderPlaced.userId,
                            orderId = orderId,
                            status = orderPlaced.status,
                            userName = orderPlaced.userName
                        )
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

    suspend fun cancelOrder(
        orderId: String,
        userId: String,
        userName: String,
        cancelStatus: String,
        status: String
    ): OrderStatusUIState {
        return when (val result = orderRepository.cancelOrder(orderId, cancelStatus, status)) {
            is OrderResult.OrderCancelSuccess -> {
                when (val notifyResult = notifyOwner(
                    userId = userId,
                    orderId = orderId,
                    status = cancelStatus,
                    userName = userName
                )) {
                    is NotificationResult.Success -> {
                        Log.d("OrderUseCase", notifyResult.message)
                    }

                    is NotificationResult.Failure -> {
                        Log.e(
                            "OrderUseCase",
                            notifyResult.exception.message,
                            notifyResult.exception
                        )
                        senderNotificationWorkerScheduler.retryNotification(
                            userId = userId,
                            orderId = orderId,
                            status = cancelStatus,
                            userName = userName
                        )
                    }
                }
                OrderStatusUIState.Success(result.isSuccess)
            }

            is OrderResult.Failure -> {
                OrderStatusUIState.CancelOrderFailure(
                    failure = result.e.toWriteReqDomainFailure(
                        orderId
                    )
                )
            }

            else -> OrderStatusUIState.Idle
        }
    }

    private suspend fun notifyOwner(
        userId: String,
        orderId: String,
        status: String,
        userName: String
    ): NotificationResult {
        val data = NotificationRequest(
            userId = userId,
            orderId = orderId,
            status = status,
            userName = userName
        )

        return notificationRepository.sendNotification(data)
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

    fun observeOrderById(orderId: String): Flow<OrderStatusUIState> {
        return orderRepository.observeOrderById(orderId)
            .map { result ->
                when (result) {
                    is OrderResult.OrderGetSuccessByOrderId -> {
                        OrderStatusUIState.OrderGetSuccess(result.order)
                    }

                    is OrderResult.Failure -> {
                        OrderStatusUIState.OrderGetFailure(result.e.toGetReqDomainFailure(orderId))
                    }

                    else -> OrderStatusUIState.Idle
                }
            }.catch {
                emit(OrderStatusUIState.OrderGetFailure(it.toGetReqDomainFailure(orderId)))
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