package com.example.roti999.domain.repository

import com.example.roti999.data.model.User


interface UserRepository {
    suspend fun createUser(user: User, onResult: (Boolean) -> Unit)

    suspend fun getUserByPhoneNumber(onResult: (User?) -> Unit)
}