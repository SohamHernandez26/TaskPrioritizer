package com.example.taskprioritizer.repository.impl

import com.example.taskprioritizer.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val auth: FirebaseAuth
) : AuthRepository {

    override val currentUserId: Flow<String?> =
        callbackFlow {

            val listener =
                FirebaseAuth.AuthStateListener { firebaseAuth ->
                    trySend(firebaseAuth.currentUser?.uid)
                }

            auth.addAuthStateListener(listener)

            awaitClose {
                auth.removeAuthStateListener(listener)
            }
        }

    override suspend fun signIn(
        email: String,
        password: String
    ): Result<Unit> =
        try {
            auth.signInWithEmailAndPassword(
                email.trim(),
                password
            ).await()

            Result.success(Unit)

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun signUp(
        email: String,
        password: String
    ): Result<Unit> =
        try {
            auth.createUserWithEmailAndPassword(
                email.trim(),
                password
            ).await()

            Result.success(Unit)

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun signOut() {
        auth.signOut()
    }
}