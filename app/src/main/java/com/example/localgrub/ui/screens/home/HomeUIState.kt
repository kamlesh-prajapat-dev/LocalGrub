package com.example.localgrub.ui.screens.home

import com.example.localgrub.data.model.GetUser
import com.example.localgrub.data.model.FoodItem
import com.example.localgrub.data.model.GetOffer
import com.example.localgrub.domain.model.failure.GetReqDomainFailure

sealed interface HomeUIState {
    object Idle : HomeUIState
    object Loading : HomeUIState
    data class DishGetSuccess(val dishes: List<FoodItem>) : HomeUIState
    data class FirebaseGetFailure(val failure: com.example.localgrub.domain.mapper.firebase.GetReqDomainFailure) :
        HomeUIState

    object NoInternet : HomeUIState
    data class FirestoreGetFailure(val failure: GetReqDomainFailure) : HomeUIState
    object LoginState : HomeUIState
    data class OrderState(val user: GetUser) : HomeUIState
    data class ProfileState(val user: GetUser) : HomeUIState
    data class OfferGetSuccess(val offers: List<GetOffer>) : HomeUIState
}