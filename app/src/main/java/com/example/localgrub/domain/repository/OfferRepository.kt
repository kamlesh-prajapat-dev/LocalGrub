package com.example.localgrub.domain.repository

import com.example.localgrub.domain.model.result.OfferResult
import kotlinx.coroutines.flow.Flow

interface OfferRepository {
    fun getOffer(): Flow<OfferResult>
}