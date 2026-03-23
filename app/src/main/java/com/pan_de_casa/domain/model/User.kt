package com.pan_de_casa.domain.model

enum class UserRole {
    CLIENT,
    ADMIN
}

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val role: UserRole = UserRole.CLIENT,
    val address: String = "",
    val phone: String = ""
)
