package com.example.localgrub.domain.repository

import com.example.localgrub.data.model.GetUser
import com.example.localgrub.data.model.NewUser
import com.example.localgrub.domain.model.result.UserResult

interface UserRepository {
    suspend fun getUserByPhoneNumber(phoneNumber: String): UserResult
    suspend fun getUserByUid(uid: String): UserResult
    suspend fun saveUser(
        user: NewUser,
        uid: String
    ): UserResult

    suspend fun saveUser(
        user: NewUser
    ): UserResult
}