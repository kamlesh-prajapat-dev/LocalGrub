package com.example.localgrub.data.remote.repository

import com.example.localgrub.data.model.firebase.FetchedOrder
import com.example.localgrub.data.model.firebase.PlacedOrder
import com.example.localgrub.data.remote.mapper.ErrorMapper
import com.example.localgrub.domain.model.result.OrderResult
import com.example.localgrub.domain.repository.OrderRepository
import com.example.localgrub.util.DataNotFoundException
import com.example.localgrub.util.OrderFields
import com.example.localgrub.util.OrderStatus
import com.example.localgrub.util.UnknownException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val realTimeDatabase: FirebaseDatabase
) : OrderRepository {
    override suspend fun placeOrder(
        orderPlaced: PlacedOrder
    ): OrderResult {
        return try {
            if(orderPlaced.items.isEmpty()) {
                OrderResult.Failure(UnknownException("Order must contain at least one item"))
            }

            val ordersRef = realTimeDatabase
                .getReference(OrderFields.COLLECTION)

            val orderId = ordersRef.push().key
                ?: return OrderResult.Failure(
                    IllegalStateException("Failed to generate orderId")
                )

            val finalOrder = orderPlaced.copy(
                placeAt = System.currentTimeMillis(),
                status = OrderStatus.PLACED
            )

            ordersRef
                .child(orderId)
                .setValue(finalOrder)
                .await()

            OrderResult.OrderCreateSuccess(
                orderPlaced = finalOrder,
                docId = orderId
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            OrderResult.Failure(e)
        }
    }

    override fun observeOrders(userId: String): Flow<OrderResult> = callbackFlow {

        val ref = realTimeDatabase.getReference(OrderFields.COLLECTION)
        val query = ref.orderByChild(OrderFields.USER_ID).equalTo(userId)

        val listener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                runCatching {
                    snapshot.children.mapNotNull { child ->
                        child.getValue(FetchedOrder::class.java)
                            ?.copy(id = child.key.orEmpty())
                    }
                }.onSuccess { orders ->
                    trySend(OrderResult.OrdersGetSuccess(orders))
                        .onFailure { throwable ->
                            // Channel closed or cancelled — safe to ignore or log
                        }
                }.onFailure { throwable ->
                    trySend(OrderResult.Failure(throwable as Exception))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(ErrorMapper.map(error))
            }
        }

        query.addValueEventListener(listener)

        awaitClose {
            query.removeEventListener(listener)
        }
    }

    override fun observeOrderById(orderId: String): Flow<OrderResult> = callbackFlow {
        val ref = realTimeDatabase.getReference(OrderFields.COLLECTION)
        val query = ref.child(orderId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                runCatching {
                    snapshot.getValue(FetchedOrder::class.java)?.copy(id = orderId)
                }.onSuccess { order ->
                    if (order != null) {
                        trySend(OrderResult.OrderGetSuccessByOrderId(order))
                    } else {
                        trySend(OrderResult.Failure(DataNotFoundException("Order not found")))
                    }.onFailure { throwable ->
                        // Channel closed or cancelled — safe to ignore or log
                    }
                }.onFailure { throwable ->
                    trySend(OrderResult.Failure(throwable as Exception))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(ErrorMapper.map(error))
            }
        }

        query.addValueEventListener(listener)
        awaitClose {
            query.removeEventListener(listener)
        }
    }

    override suspend fun cancelOrder(
        orderId: String,
        cancelStatus: String,
        status: String
    ): OrderResult {
        if (orderId.isBlank()) {
            return OrderResult.Failure(IllegalArgumentException("OrderId cannot be blank"))
        }

        val orderRef = realTimeDatabase
            .getReference(OrderFields.COLLECTION)
            .child(orderId)

        return try {

            val snapshot = orderRef.get().await()
            if (!snapshot.exists()) {
                return OrderResult.Failure(
                    IllegalStateException("Order not found")
                )
            }

            val updates = mapOf<String, Any>(
                OrderFields.STATUS to cancelStatus,
                OrderFields.PREVIOUS_STATUS to status,
                OrderFields.CANCELLED_AT to System.currentTimeMillis()
            )

            orderRef.updateChildren(updates).await()

            OrderResult.OrderCancelSuccess(true)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            OrderResult.Failure(e)
        }
    }
}