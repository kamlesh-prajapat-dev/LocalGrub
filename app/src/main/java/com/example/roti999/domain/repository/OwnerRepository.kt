package com.example.roti999.domain.repository

import com.example.roti999.domain.model.OwnerResult

interface OwnerRepository {
    suspend fun getOwnerFcmToken(): OwnerResult
}