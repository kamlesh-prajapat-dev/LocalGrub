package com.example.roti999.domain.model

sealed interface OwnerResult {
    data class Success(val token: String): OwnerResult
    data class Error(val e: Exception): OwnerResult
}