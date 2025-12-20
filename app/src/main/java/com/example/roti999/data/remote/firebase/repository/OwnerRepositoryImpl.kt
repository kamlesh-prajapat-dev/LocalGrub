package com.example.roti999.data.remote.firebase.repository

import com.example.roti999.domain.model.OwnerResult
import com.example.roti999.domain.repository.OwnerRepository
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