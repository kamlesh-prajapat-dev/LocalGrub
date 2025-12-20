package com.example.roti999.data.remote.firebase.repository

import com.example.roti999.data.local.LocalDatabase
import com.example.roti999.data.model.User
import com.example.roti999.domain.model.UserResult
import com.example.roti999.domain.repository.UserRepository
import com.example.roti999.ui.screens.createprofile.ProfileUIState
import com.example.roti999.util.TokenManager
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

    override suspend fun createUser(user: User): UserResult {
        return try {
            firestore.collection("users").document(user.uid).set(user).await()
            UserResult.Success(user)
        } catch (e: Exception) {
            UserResult.Error(e)
        }
    }

    override suspend fun getUserByPhoneNumber(): UserResult {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val snapshot = FirebaseFirestore.getInstance()
                    .collection("users")
                    .whereEqualTo("phoneNumber", currentUser.phoneNumber)
                    .limit(1)
                    .get()
                    .await()

                val documents = snapshot.documents
                if (documents.isNotEmpty()) {
                    val user = documents[0].toObject(User::class.java)
                    UserResult.Success(user)
                } else {
                    UserResult.Success(null)
                }
            } else {
                UserResult.NavigateToLogin
            }
        } catch (e: Exception) {
            UserResult.Error(e)
        }
    }

    override fun getCurrentUser(): User? {
        val currentUser = auth.currentUser ?: return null

        return User(
            uid = currentUser.uid,
            phoneNumber = currentUser.phoneNumber.orEmpty()
        )
    }

    override suspend fun saveNewToken(
        user: User
    ): UserResult {
        return try {
            firestore.collection("users")
                .document(user.uid)
                .update("fcmToken", user.fcmToken)
                .await()
            UserResult.Success(user)
        } catch (e: Exception) {
            UserResult.Error(e)
        }
    }

    override fun logout() {
        auth.signOut()
    }
}