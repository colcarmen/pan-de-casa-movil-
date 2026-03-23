package com.pan_de_casa.data.repository

import com.pan_de_casa.domain.model.User
import com.pan_de_casa.domain.model.UserRole
import com.pan_de_casa.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor() : AuthRepository {

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: Flow<User?> = _currentUser.asStateFlow()

    override suspend fun login(email: String, password: String): Result<User> {
        // Simulación de login según SRS
        return if (email == "admin@pandecasa.com" && password == "admin123") {
            val user = User(
                uid = "admin_uid",
                email = email,
                name = "Administrador",
                role = UserRole.ADMIN
            )
            _currentUser.value = user
            Result.success(user)
        } else if (email == "cliente@pandecasa.com" && password == "cliente123") {
            val user = User(
                uid = "client_uid",
                email = email,
                name = "Cliente de Prueba",
                role = UserRole.CLIENT,
                address = "Calle Falsa 123",
                phone = "555-1234"
            )
            _currentUser.value = user
            Result.success(user)
        } else {
            Result.failure(Exception("Credenciales inválidas para la demo"))
        }
    }

    override suspend fun logout() {
        _currentUser.value = null
    }

    override suspend fun getCurrentUserRole(): String? {
        return _currentUser.value?.role?.name
    }
}
