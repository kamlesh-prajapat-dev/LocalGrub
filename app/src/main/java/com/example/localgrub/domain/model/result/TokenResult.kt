package com.example.localgrub.domain.model.result

import com.example.localgrub.data.model.TokenData

interface TokenResult {
    data class TokenUpdateSuccess(val success: Boolean) : TokenResult
    data class TokenGetSuccess(val tokenData: TokenData) : TokenResult
    data class Failure(val exception: Exception) : TokenResult
}