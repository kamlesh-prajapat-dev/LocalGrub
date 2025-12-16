package com.example.roti999.domain.repository

import com.example.roti999.data.dto.User
import com.example.roti999.ui.screens.createprofile.ProfileUIState

interface UserRepository {
    suspend fun createUser(user: User, onResult: (ProfileUIState) -> Unit)
    suspend fun getUserByPhoneNumber(onResult: (User?) -> Unit)
    suspend fun getCurrentUser(): User?
    suspend fun saveNewToken(userWithFCMToken: User, onResult: (Boolean) -> Unit)
    suspend fun logout()
}