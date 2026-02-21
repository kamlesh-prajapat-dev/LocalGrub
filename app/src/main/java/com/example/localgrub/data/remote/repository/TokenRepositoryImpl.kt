package com.example.localgrub.data.remote.repository

import com.example.localgrub.data.model.TokenData
import com.example.localgrub.domain.model.result.TokenResult
import com.example.localgrub.domain.repository.TokenRepository
import com.example.localgrub.util.DataNotFoundException
import com.example.localgrub.util.TokenRepositoryConstant
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class TokenRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
): TokenRepository {
    override suspend fun saveToken(
        token: String,
        docId: String
    ): TokenResult {
        if (token.isBlank()) {
            return TokenResult.Failure(
                IllegalArgumentException("FCM token cannot be empty")
            )
        }

        if (docId.isBlank()) {
            return TokenResult.Failure(
                IllegalArgumentException("FCM token cannot be empty")
            )
        }

        return try {
            val tokenData = mapOf(
                "token" to token,
                "updatedAt" to System.currentTimeMillis(),
                "platform" to "Android"
            )

            firestore
                .collection(TokenRepositoryConstant.TOKENS_COLLECTION_NAME)
                .document(docId)
                .set(
                    tokenData,
                    SetOptions.merge()
                )
                .await()

            TokenResult.TokenUpdateSuccess(true)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            TokenResult.Failure(e)
        }
    }

    override suspend fun getToken(docId: String): TokenResult {
//        if (docId.isBlank()) {
//            return TokenResult.Failure(
//                IllegalArgumentException("FCM token cannot be empty")
//            )
//        }

        return try {
            val adminSnapshot = firestore.collection(TokenRepositoryConstant.ADMINS_COLLECTION_NAME)
                .limit(1)
                .get()
                .await()

            val shopId = adminSnapshot.documents.firstOrNull()?.id
            if (shopId == null) {
                return TokenResult.Failure(
                    DataNotFoundException("Shop not found")
                )
            } else {
                val snapshot = firestore.collection(TokenRepositoryConstant.TOKENS_COLLECTION_NAME)
                    .document(shopId)
                    .get()
                    .await()

                val tokenData = snapshot.toObject(TokenData::class.java)

                if (tokenData == null) {
                    TokenResult.Failure(
                        DataNotFoundException("Token not found")
                    )
                } else {
                    TokenResult.TokenGetSuccess(tokenData)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            TokenResult.Failure(e)
        }
    }
}