package com.example.localgrub.domain.model.result

sealed interface OwnerResult {
    data class Success(val token: String): OwnerResult
    data class Error(val e: Exception): OwnerResult
}