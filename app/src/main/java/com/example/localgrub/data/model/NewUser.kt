package com.example.localgrub.data.model

data class NewUser(
    val name: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val createAt: Long = 0L,
    val profileCompleted: Boolean = false
)
