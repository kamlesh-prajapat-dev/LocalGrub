package com.example.roti999.data.remote.firebase.repository

import com.example.roti999.data.model.FetchedOrder
import com.example.roti999.data.model.PlacedOrder
import com.example.roti999.domain.model.OrderResult
import com.example.roti999.domain.repository.OrderRepository
import com.example.roti999.util.OrderFields
import com.example.roti999.util.OrderStatus
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val realTimeDatabase: FirebaseDatabase
): OrderRepository {
    override suspend fun placeOrder(
        orderPlaced: PlacedOrder
    ): OrderResult {
        return try {
            val ordersRef = realTimeDatabase
                .getReference(OrderFields.COLLECTION)

            // Firebase generates unique orderId
            val orderId = ordersRef.push().key
                ?: throw IllegalStateException("OrderId not generated")

            // Save order
            ordersRef.child(orderId)
                .setValue(orderPlaced)
                .await()

            OrderResult.Success(orderPlaced, orderId)
        } catch (e: Exception) {
            OrderResult.Error(e)
        }
    }
    override fun observeOrders(userId: String): Flow<OrderResult> = callbackFlow {

        val ref = realTimeDatabase.getReference(OrderFields.COLLECTION)
        val query = ref.orderByChild(OrderFields.USER_ID).equalTo(userId)

        val listener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val orders = snapshot.children.mapNotNull { child ->
                    child.getValue(FetchedOrder::class.java)
                        ?.copy(id = child.key ?: "")
                }
                trySend(OrderResult.OrdersSuccess(orders))
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
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
        return try {
            val updates = mapOf<String, Any>(
                OrderFields.STATUS to cancelStatus,
                OrderFields.PREVIOUS_STATUS to status
            )

            realTimeDatabase
                .getReference(OrderFields.COLLECTION)
                .child(orderId)
                .updateChildren(updates)
                .await()

            OrderResult.OrderCancelSuccess(true)
        } catch (e: Exception) {
            OrderResult.Error(e)
        }
    }
}