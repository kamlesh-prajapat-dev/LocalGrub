package com.example.roti999.domain.usecase

import com.example.roti999.data.local.LocalDatabase
import com.example.roti999.data.model.User
import com.example.roti999.domain.model.UserResult
import com.example.roti999.domain.repository.UserRepository
import com.example.roti999.ui.screens.createprofile.ProfileUIState
import com.example.roti999.ui.screens.home.HomeUIState
import com.example.roti999.util.TokenManager
import javax.inject.Inject

class UserUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val localDatabase: LocalDatabase
) {
    suspend fun createUser(user: User): ProfileUIState {
        val token = TokenManager.getFCMToken()
        var userWithToken: User = user
        if (token != null) {
            userWithToken = user.copy(fcmToken = token)
        }

        return when(val userResult = userRepository.createUser(userWithToken)) {
            is UserResult.Success -> {
                localDatabase.setUser(userResult.user)
                ProfileUIState.UserSavedSuccess(user)
            }
            is UserResult.Error -> {
                ProfileUIState.Failure(userResult.e)
            }
            else -> ProfileUIState.Idle
        }
    }

    fun getCurrentUser() = userRepository.getCurrentUser()

    suspend fun getUserByPhoneNumber(): HomeUIState {
        return when(val userResult = userRepository.getUserByPhoneNumber()) {
            is UserResult.Success -> {
                var user = userResult.user
                if(userResult.user != null) {
                    val token = TokenManager.getFCMToken()
                    if (token != null) {
                        user = user.copy(fcmToken = token)
                        userRepository.saveNewToken(user)
                    }
                }
                localDatabase.setUser(user)
                HomeUIState.SuccessUser(user)
            }

            is UserResult.Error -> {
                HomeUIState.Error(userResult.e)
            }

            UserResult.NavigateToLogin -> {
                HomeUIState.NavigateToLogin
            }
        }
    }

    suspend fun saveNewToken(token: String) {
        val user = localDatabase.getUser()
        if (user != null) {
            val newUser = user.copy(fcmToken = token)
            userRepository.saveNewToken(newUser)
        }
    }

    fun logout() = userRepository.logout()
}