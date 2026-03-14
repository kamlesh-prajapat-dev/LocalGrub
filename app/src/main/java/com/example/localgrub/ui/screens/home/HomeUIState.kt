package com.example.localgrub.ui.screens.home

import com.example.localgrub.data.model.firebase.GetUser
import com.example.localgrub.data.model.firebase.FoodItem
import com.example.localgrub.data.model.firebase.GetOffer
import com.example.localgrub.domain.model.failure.GetReqDomainFailure

sealed interface HomeUIState {
    object Idle : HomeUIState
    object Loading : HomeUIState
    object NoInternet : HomeUIState
    object LoginState : HomeUIState

    data class DishGetSuccess(val dishes: List<FoodItem>) : HomeUIState
    data class OfferGetSuccess(val offers: List<GetOffer>) : HomeUIState
    data class FirebaseGetFailure(val failure: com.example.localgrub.domain.mapper.firebase.GetReqDomainFailure) :
        HomeUIState
    data class FirestoreGetFailure(val failure: GetReqDomainFailure) : HomeUIState
    data class OrderState(val user: GetUser) : HomeUIState
    data class ProfileState(val user: GetUser) : HomeUIState
}