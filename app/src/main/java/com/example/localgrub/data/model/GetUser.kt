package com.example.localgrub.data.model

import java.io.Serializable

@kotlinx.serialization.Serializable
data class GetUser(
    val uid: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val createAt: Long = 0L,
    val profileCompleted: Boolean = false
): Serializable