package com.example.roti999.ui.screens.createprofile

import com.example.roti999.data.model.User

sealed interface ProfileUIState {

    object Idle : ProfileUIState
    object Loading : ProfileUIState
    data class UserSavedSuccess(val user: User): ProfileUIState
    object Success : ProfileUIState
    data class Failure(val e: Exception) : ProfileUIState
    data class ValidationErrors(val errors: String) : ProfileUIState

    object NavigateToLogin: ProfileUIState
}