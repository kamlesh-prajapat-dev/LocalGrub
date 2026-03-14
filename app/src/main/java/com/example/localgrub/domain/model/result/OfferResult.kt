package com.example.localgrub.domain.model.result

import com.example.localgrub.data.model.firebase.GetOffer

sealed interface OfferResult {
    data class GetSuccess(val offers: List<GetOffer>): OfferResult
    data class Failure(val failure: Exception): OfferResult
}