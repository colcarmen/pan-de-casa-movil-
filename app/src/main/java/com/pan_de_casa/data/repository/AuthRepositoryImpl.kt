package com.pan_de_casa.data.repository

import com.pan_de_casa.domain.model.User
import com.pan_de_casa.domain.model.UserRole
import com.pan_de_casa.domain.repository.AuthRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val supabaseAuth: Auth
) : AuthRepository {

    override val currentUser: Flow<User?> = supabaseAuth.sessionStatus
        .map { _ -> 
            supabaseAuth.currentUserOrNull()?.toDomain() 
        }
        .distinctUntilChanged()

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            supabaseAuth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            val user = supabaseAuth.currentUserOrNull()?.toDomain()
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("No se pudo recuperar el perfil del usuario"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUp(
        email: String,
        password: String,
        name: String,
        role: String,
        address: String,
        phone: String
    ): Result<Unit> {
        return try {
            supabaseAuth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject {
                    put("name", name)
                    put("role", role)
                    put("address", address)
                    put("phone", phone)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        try {
            supabaseAuth.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun getCurrentUserRole(): String? {
        val user = supabaseAuth.currentUserOrNull()
        return user?.userMetadata?.get("role")?.toString()?.replace("\"", "")
    }

    private fun UserInfo.toDomain(): User {
        val roleStr = userMetadata?.get("role")?.toString()?.replace("\"", "") ?: "CLIENT"
        return User(
            uid = id,
            email = email ?: "",
            name = userMetadata?.get("name")?.toString()?.replace("\"", "") ?: "",
            role = if (roleStr == "ADMIN") UserRole.ADMIN else UserRole.CLIENT,
            address = userMetadata?.get("address")?.toString()?.replace("\"", "") ?: "",
            phone = userMetadata?.get("phone")?.toString()?.replace("\"", "") ?: ""
        )
    }
}
