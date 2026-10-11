package com.pan_de_casa.domain.repository

import com.pan_de_casa.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun signUp(
        email: String,
        password: String,
        name: String,
        role: String = "CLIENT",
        address: String = "",
        phone: String = ""
    ): Result<Unit>
    suspend fun logout()
    suspend fun getCurrentUserRole(): String?
}
