package com.example.taskprioritizer.repository

import com.example.taskprioritizer.data.local.ReminderEntity
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {

    fun observeReminders(
        ownerId: String
    ): Flow<List<ReminderEntity>>

    suspend fun saveReminder(
        reminder: ReminderEntity
    )

    suspend fun deleteReminder(
        id: Long,
        ownerId: String
    )
}