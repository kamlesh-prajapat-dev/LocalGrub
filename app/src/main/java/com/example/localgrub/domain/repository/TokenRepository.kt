package com.example.localgrub.domain.repository

import com.example.localgrub.domain.model.result.TokenResult

interface TokenRepository {
    suspend fun saveToken(
        token: String,
        docId: String
    ): TokenResult

    suspend fun getToken(
        docId: String
    ): TokenResult
}