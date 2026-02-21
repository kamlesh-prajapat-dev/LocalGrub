package com.example.localgrub.data.remote.repository

import com.example.localgrub.domain.model.result.OwnerResult
import com.example.localgrub.domain.repository.OwnerRepository
import com.google.firebase.firestore.FirebaseFirestore
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.tasks.await

@Singleton
class OwnerRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : OwnerRepository {
    override suspend fun getOwnerFcmToken(): OwnerResult {
        return try {
            val snapshot = firestore.collection("owners")
                .get()
                .await()
            val token = snapshot.documents.firstOrNull()
                ?.getString("token")
                .orEmpty()
            OwnerResult.Success(token)
        } catch (e: Exception) {
            OwnerResult.Error(e)
        }
    }
}