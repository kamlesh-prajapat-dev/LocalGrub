package com.example.localgrub.domain.repository

import com.example.localgrub.domain.model.result.OwnerResult

interface OwnerRepository {
    suspend fun getOwnerFcmToken(): OwnerResult
}