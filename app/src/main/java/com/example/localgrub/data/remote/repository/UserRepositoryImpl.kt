package com.example.localgrub.data.remote.repository

import com.example.localgrub.data.model.NewUser
import com.example.localgrub.domain.model.result.UserResult
import com.example.localgrub.domain.repository.UserRepository
import com.example.localgrub.util.DataNotFoundException
import com.example.localgrub.util.UserRepositoryConstant
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository {

    override suspend fun saveUser(user: NewUser, uid: String): UserResult {
        if (uid.isBlank()) {
            return UserResult.Failure(
                IllegalArgumentException("User id cannot be blank")
            )
        }

        return try {
            firestore.collection(UserRepositoryConstant.USERS_COLLECTION_NAME)
                .document(uid)
                .set(user)
                .await()

            UserResult.Success(user, uid)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            UserResult.Failure(e)
        }
    }

    override suspend fun saveUser(user: NewUser): UserResult {
        return try {
            val documentReference = firestore.collection(UserRepositoryConstant.USERS_COLLECTION_NAME)
                .add(user)
                .await()

            UserResult.Success(user = user, uid = documentReference.id)
        } catch (e: Exception) {
            UserResult.Failure(e)
        }
    }

    override suspend fun getUserByPhoneNumber(phoneNumber: String): UserResult {
        if (phoneNumber.isBlank()) {
            return UserResult.Failure(
                IllegalArgumentException("Phone number cannot be blank")
            )
        }

        return try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection(UserRepositoryConstant.USERS_COLLECTION_NAME)
                .whereEqualTo(UserRepositoryConstant.PHONE_NUMBER, phoneNumber)
                .limit(1)
                .get()
                .await()

            val document = snapshot.documents.firstOrNull()

            val user = document?.toObject(NewUser::class.java)
                ?: return UserResult.Failure(
                    DataNotFoundException("User not found")
                )

            UserResult.Success(user = user, document.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            UserResult.Failure(e)
        }
    }

    override suspend fun getUserByUid(uid: String): UserResult {
        if (uid.isBlank()) {
            return UserResult.Failure(
                IllegalArgumentException("Uid cannot be blank")
            )
        }

        return try {
            val document = FirebaseFirestore.getInstance()
                .collection(UserRepositoryConstant.USERS_COLLECTION_NAME)
                .document(uid)
                .get()
                .await()

            val user = document?.toObject(NewUser::class.java)
                ?: return UserResult.Failure(
                    DataNotFoundException("User not found")
                )


            UserResult.Success(user, uid)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            UserResult.Failure(e)
        }
    }
}