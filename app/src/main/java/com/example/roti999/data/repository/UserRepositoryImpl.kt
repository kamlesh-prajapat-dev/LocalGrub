package com.example.roti999.data.repository

import com.example.roti999.data.local.LocalDatabase
import com.example.roti999.domain.model.User
import com.example.roti999.domain.repository.UserRepository
import com.example.roti999.util.TokenManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val localDatabase: LocalDatabase
) : UserRepository {

    override suspend fun createUser(user: User, onResult: (User?) -> Unit) {
        try {
            val token = TokenManager.getFCMToken()
            if (token != null) {
                val userWithFCMToken = com.example.roti999.data.model.User(
                    uid = user.uid,
                    name = user.name,
                    phoneNumber = user.phoneNumber,
                    address = user.address,
                    fcmToken = token
                )
                firestore.collection("users").document(user.uid).set(userWithFCMToken).await()
                localDatabase.setUser(user.copy(fcmToken = token))
                onResult(user.copy(fcmToken = token))
            }
        } catch (e: Exception) {
            onResult(null)
        }
    }

    override suspend fun getUserByPhoneNumber(onResult: (User?) -> Unit) {
        try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val snapshot = FirebaseFirestore.getInstance()
                    .collection("users")
                    .whereEqualTo("phoneNumber", currentUser.phoneNumber)
                    .limit(1)
                    .get()
                    .await()

                if (!snapshot.isEmpty) {
                    val document = snapshot.documents[0]
                    onResult(document.toObject(User::class.java))
                }
                else
                    onResult(null)
            } else {
                onResult(null)
            }
        }catch (e: Exception) {
            onResult(null)
        }
    }

    override suspend fun getCurrentUser(onResult: (User?) -> Unit) {
        try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                onResult(
                    User(
                        uid = currentUser.uid,
                        phoneNumber = currentUser.phoneNumber ?: "",
                    )
                )
            } else {
                onResult(null)
            }
        } catch (e: Exception) {
            onResult(null)
        }
    }

    override suspend fun saveNewToken(
        userWithFCMToken: com.example.roti999.data.model.User,
        onResult: (Boolean) -> Unit
    ) {
        try {
            firestore.collection("users").document(userWithFCMToken.uid).set(userWithFCMToken).await()
            onResult(true)
        } catch (e: Exception) {
            onResult(false)
        }
    }

    override suspend fun logout() {
        auth.signOut()
    }
}