package com.example.localgrub.domain.usecase

import com.example.localgrub.data.local.LocalDatabase
import com.example.localgrub.data.model.GetUser
import com.example.localgrub.data.model.NewUser
import com.example.localgrub.domain.mapper.firestore.FirestoreFailureMapper
import com.example.localgrub.domain.mapper.firestore.FirestoreWriteFailureMapper
import com.example.localgrub.domain.model.result.UserResult
import com.example.localgrub.domain.repository.UserRepository
import com.example.localgrub.ui.screens.createprofile.ProfileUIState
import com.example.localgrub.ui.screens.home.HomeUIState
import javax.inject.Inject

class UserUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val localDatabase: LocalDatabase
) {
    fun getLocalUser() = localDatabase.getUser()
    fun setLocalUser(user: GetUser?) = localDatabase.setUser(user)

    suspend fun saveUser(user: GetUser): ProfileUIState {
        val uid = user.uid
        val newUser = convertGetUserToNewUser(user)
        return when (val result = userRepository.saveUser(user = newUser, uid = uid)) {
            is UserResult.Failure -> {
                ProfileUIState.Failure(FirestoreWriteFailureMapper.map(result.e, user))
            }

            is UserResult.Success -> {
                val user = convertNewUserToGetUser(result.user, result.uid)
                localDatabase.setUser(user)
                ProfileUIState.Success(user)
            }
        }
    }

//    suspend fun getUserByPhoneNumber(phoneNumber: String): HomeUIState {
//        return when (val result = userRepository.getUserByPhoneNumber(phoneNumber)) {
//            is UserResult.Failure -> {
//                HomeUIState.UserGetFailure(FirestoreFailureMapper.map(result.failure, phoneNumber))
//            }
//
//            is UserResult.Success -> {
//                val user = convertNewUserToGetUser(result.user, result.uid)
//                if (user.isProfileCompleted) {
//                    localDatabase.setUser(user)
//                    HomeUIState.OrderState
//                } else {
//                    HomeUIState.ProfileState
//                }
//            }
//        }
//    }

    suspend fun getUserByUid(uid: String, phoneNumber: String? = null): HomeUIState {
        return when (val result = userRepository.getUserByUid(uid)) {
            is UserResult.Failure -> {
                HomeUIState.UserGetFailure(FirestoreFailureMapper.map(result.e, uid))
            }

            is UserResult.Success -> {
                val user = convertNewUserToGetUser(result.user, result.uid)
                if (user.profileCompleted) {
                    localDatabase.setUser(user)
                    HomeUIState.OrderState(user)
                } else {
                    HomeUIState.ProfileState(GetUser(uid = uid, phoneNumber = phoneNumber!!))
                }
            }
        }
    }

    private fun convertGetUserToNewUser(getUser: GetUser): NewUser {
        return NewUser(
            name = getUser.name,
            address = getUser.address,
            phoneNumber = getUser.phoneNumber,
            createAt = getUser.createAt,
            profileCompleted = getUser.profileCompleted
        )
    }

    private fun convertNewUserToGetUser(newUser: NewUser, uid: String): GetUser {
        return GetUser(
            uid = uid,
            name = newUser.name,
            address = newUser.address,
            phoneNumber = newUser.phoneNumber,
            createAt = newUser.createAt,
            profileCompleted = newUser.profileCompleted
        )
    }
}