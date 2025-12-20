package com.example.roti999.domain.model

import com.example.roti999.data.model.User

sealed interface UserResult {
    data class Success(val user: User?) : UserResult
    data class Error(val e: Exception) : UserResult
    object NavigateToLogin : UserResult
}