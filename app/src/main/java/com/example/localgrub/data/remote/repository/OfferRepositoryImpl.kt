package com.example.localgrub.data.remote.repository

import com.example.localgrub.data.model.firebase.GetOffer
import com.example.localgrub.data.remote.mapper.ErrorMapper
import com.example.localgrub.domain.model.result.OfferResult
import com.example.localgrub.domain.repository.OfferRepository
import com.example.localgrub.util.OfferConstant
import com.example.localgrub.util.OfferStatus
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class OfferRepositoryImpl @Inject constructor(
    private val realtimeDatabase: FirebaseDatabase
): OfferRepository {
    override fun getOffer(): Flow<OfferResult> = callbackFlow {
        val ref = realtimeDatabase.getReference(OfferConstant.COLLECTION_NAME)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                runCatching {
                    val currentTime = System.currentTimeMillis()
                    snapshot.children.mapNotNull { child ->
                        child.getValue(GetOffer::class.java)
                            ?.copy(id = child.key.orEmpty())
                    }.filter { offer ->
                        offer.offerStatus == OfferStatus.ACTIVE &&
                                offer.expiryDate >= currentTime
                    }
                }.onSuccess { offers ->
                    trySend(OfferResult.GetSuccess(offers))
                        .onFailure {
                            // Channel cancelled — collector gone (normal lifecycle case)
                        }
                }.onFailure { throwable ->
                    trySend(OfferResult.Failure(throwable as Exception))
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