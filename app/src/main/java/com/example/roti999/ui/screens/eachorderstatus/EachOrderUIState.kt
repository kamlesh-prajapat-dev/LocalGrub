package com.example.roti999.ui.screens.eachorderstatus

import com.example.roti999.data.model.FetchedOrder

sealed interface EachOrderUIState {
    object Idle: EachOrderUIState
    object Loading: EachOrderUIState
    data class Success(val isSuccess: Boolean): EachOrderUIState
    data class Failure(val exception: Exception): EachOrderUIState
    data class OrderGetSuccess(val order: FetchedOrder): EachOrderUIState
    object NoInternet: EachOrderUIState
}