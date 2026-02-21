package com.example.localgrub.data.remote.repository

import com.example.localgrub.data.model.FetchedDish
import com.example.localgrub.data.remote.mapper.ErrorMapper
import com.example.localgrub.domain.model.result.DishResult
import com.example.localgrub.domain.repository.DishesRepository
import com.example.localgrub.util.DishFields
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DishesRepositoryImpl @Inject constructor(
    private val realtimeDatabase: FirebaseDatabase
) : DishesRepository {
    override fun observeDishes(): Flow<DishResult> = callbackFlow {

        val ref = realtimeDatabase.getReference(DishFields.COLLECTION)

        val listener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                runCatching {
                    snapshot.children.mapNotNull { child ->
                        child.getValue(FetchedDish::class.java)
                            ?.copy(id = child.key.orEmpty())
                    }
                }.onSuccess { dishes ->
                    trySend(DishResult.Success(dishes))
                        .onFailure {
                            // Channel cancelled — collector gone (normal lifecycle case)
                        }
                }.onFailure { throwable ->
                    trySend(DishResult.Error(throwable as Exception))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(ErrorMapper.map(error))
            }
        }

        ref.addValueEventListener(listener)

        awaitClose {
            ref.removeEventListener(listener)
        }
    }
}