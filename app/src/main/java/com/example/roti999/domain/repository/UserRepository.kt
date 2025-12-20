package com.example.roti999.domain.repository

import com.example.roti999.data.model.User
import com.example.roti999.domain.model.UserResult
import com.example.roti999.ui.screens.createprofile.ProfileUIState

interface UserRepository {
    suspend fun createUser(user: User): UserResult
    suspend fun getUserByPhoneNumber(): UserResult
    fun getCurrentUser(): User?
    suspend fun saveNewToken(user: User): UserResult
    fun logout()
}