package com.example.roti999.ui.screens.eachorderstatus

sealed interface EachOrderUIState {
    object Idle: EachOrderUIState
    object Loading: EachOrderUIState
    data class Success(val isSuccess: Boolean): EachOrderUIState
    data class Failure(val exception: Exception): EachOrderUIState
}