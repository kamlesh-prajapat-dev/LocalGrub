package com.example.localgrub.domain.model.result

import com.example.localgrub.data.model.api.request.TokenRequest

interface TokenResult {
    data class TokenUpdateSuccess(val success: Boolean) : TokenResult
    data class TokenGetSuccess(val tokenData: TokenRequest) : TokenResult
    data class Failure(val exception: Exception) : TokenResult
}