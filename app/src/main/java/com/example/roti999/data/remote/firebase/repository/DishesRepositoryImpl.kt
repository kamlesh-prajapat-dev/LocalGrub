package com.example.roti999.data.remote.firebase.repository

import com.example.roti999.data.model.FetchedDish
import com.example.roti999.domain.model.DishResult
import com.example.roti999.domain.repository.DishesRepository
import com.example.roti999.util.DishFields
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
class DishesRepositoryImpl @Inject constructor(
    private val realtimeDatabase: FirebaseDatabase
): DishesRepository {
    override fun observeDishes(): Flow<DishResult> = callbackFlow {

        val ref = realtimeDatabase.getReference(DishFields.COLLECTION)
        val query = ref.orderByChild(DishFields.IN_STOCK).equalTo(true)

        val listener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val dishes = snapshot.children.mapNotNull { child ->
                    child.getValue(FetchedDish::class.java)
                        ?.copy(id = child.key ?: "")
                }
                trySend(DishResult.Success(dishes))
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
}