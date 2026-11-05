package com.example.taskprioritizer.repository.impl

import com.example.taskprioritizer.repository.AuthRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

class FirebaseAuthRepository(private val auth: FirebaseAuth) : AuthRepository {
    override val currentUserId: Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.uid) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signIn(email: String, password: String): Result<Unit> = authenticate {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
    }

    override suspend fun signUp(email: String, password: String): Result<Unit> = authenticate {
        auth.createUserWithEmailAndPassword(email.trim(), password).await()
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    private suspend fun authenticate(action: suspend () -> Unit): Result<Unit> = try {
        val completed = withTimeoutOrNull(AUTH_TIMEOUT_MILLIS) {
            action()
            true
        } ?: false
        if (completed) {
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException("Se agotó el tiempo de espera. Revisa tu conexión y vuelve a intentar."))
        }
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        val message = when (exception) {
            is FirebaseNetworkException -> "Necesitas conexión para entrar o crear tu cuenta."
            is FirebaseAuthException -> when (exception.errorCode) {
                "ERROR_EMAIL_ALREADY_IN_USE" -> "Este correo ya tiene una cuenta. Usa Iniciar sesión."
                "ERROR_WEAK_PASSWORD" -> "La contraseña no cumple la política del proyecto."
                "ERROR_OPERATION_NOT_ALLOWED" -> "Activa Correo/contraseña en Firebase Authentication."
                "ERROR_TOO_MANY_REQUESTS" -> "Se hicieron demasiados intentos. Espera unos minutos."
                else -> "No se pudo acceder. Revisa el correo y la contraseña."
            }
            else -> "No se pudo acceder. Revisa la conexión e intenta nuevamente."
        }
        Result.failure(IllegalStateException(message, exception))
    }

    private companion object {
        const val AUTH_TIMEOUT_MILLIS = 15000L
    }
}
