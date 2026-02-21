package com.example.localgrub.domain.model.result

import com.example.localgrub.data.model.NewUser

sealed interface UserResult {
    data class Success(val user: NewUser, val uid: String) : UserResult
    data class Failure(val e: Exception) : UserResult
}