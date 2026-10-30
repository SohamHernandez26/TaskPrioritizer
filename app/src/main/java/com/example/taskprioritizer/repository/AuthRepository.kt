package com.example.taskprioritizer.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    val currentUserId: Flow<String?>

    suspend fun signIn(
        email: String,
        password: String
    ): Result<Unit>

    suspend fun signUp(
        email: String,
        password: String
    ): Result<Unit>

    suspend fun signOut()
}
