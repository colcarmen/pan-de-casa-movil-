package com.pan_de_casa.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pan_de_casa.domain.model.User
import com.pan_de_casa.domain.model.UserRole
import com.pan_de_casa.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override val currentUser: Flow<User?> = callbackFlow {
        val authListener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser == null) {
                trySend(null)
            } else {
                firestore.collection("users").document(firebaseUser.uid)
                    .get()
                    .addOnSuccessListener { doc ->
                        val roleStr = doc.getString("role") ?: "CLIENT"
                        val user = User(
                            uid = firebaseUser.uid,
                            email = firebaseUser.email ?: "",
                            name = doc.getString("name") ?: (firebaseUser.displayName ?: ""),
                            role = if (roleStr.equals("ADMIN", ignoreCase = true)) UserRole.ADMIN else UserRole.CLIENT,
                            address = doc.getString("address") ?: "",
                            phone = doc.getString("phone") ?: ""
                        )
                        trySend(user)
                    }
                    .addOnFailureListener {
                        trySend(
                            User(
                                uid = firebaseUser.uid,
                                email = firebaseUser.email ?: "",
                                name = firebaseUser.displayName ?: "",
                                role = UserRole.CLIENT
                            )
                        )
                    }
            }
        }
        firebaseAuth.addAuthStateListener(authListener)
        awaitClose { firebaseAuth.removeAuthStateListener(authListener) }
    }.distinctUntilChanged()

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: return Result.failure(Exception("No se pudo iniciar sesión"))
            val doc = firestore.collection("users").document(firebaseUser.uid).get().await()
            val roleStr = doc.getString("role") ?: "CLIENT"
            val user = User(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                name = doc.getString("name") ?: (firebaseUser.displayName ?: ""),
                role = if (roleStr.equals("ADMIN", ignoreCase = true)) UserRole.ADMIN else UserRole.CLIENT,
                address = doc.getString("address") ?: "",
                phone = doc.getString("phone") ?: ""
            )
            Result.success(user)
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
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: return Result.failure(Exception("Error al crear la cuenta"))
            val userData = hashMapOf(
                "uid" to firebaseUser.uid,
                "email" to email,
                "name" to name,
                "role" to role.uppercase(),
                "address" to address,
                "phone" to phone,
                "createdAt" to System.currentTimeMillis()
            )
            firestore.collection("users").document(firebaseUser.uid).set(userData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        try {
            firebaseAuth.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun getCurrentUserRole(): String? {
        val uid = firebaseAuth.currentUser?.uid ?: return null
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            doc.getString("role")
        } catch (e: Exception) {
            null
        }
    }
}
