package com.example.roti999.data.repository

import com.example.roti999.data.model.User
import com.example.roti999.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : UserRepository {

    override suspend fun createUser(user: User, onResult: (Boolean) -> Unit) {
        try {
            firestore.collection("users").document(user.uid).set(user).await()
            onResult(true)
        } catch (e: Exception) {
            onResult(false)
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

                if (!snapshot.isEmpty)
                    onResult(snapshot.documents[0].toObject(User::class.java))
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
                        phoneNumber = currentUser.phoneNumber ?: ""
                    )
                )
            } else {
                onResult(null)
            }
        } catch (e: Exception) {
            onResult(null)
        }
    }

    override suspend fun logout() {
        auth.signOut()
    }
}