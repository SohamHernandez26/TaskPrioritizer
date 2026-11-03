package com.example.taskprioritizer.repository

import com.example.taskprioritizer.model.Reminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun observeReminders(ownerId: String): Flow<List<Reminder>>
    suspend fun saveReminder(reminder: Reminder)
    suspend fun deleteReminder(id: Long, ownerId: String)
    suspend fun deleteForTask(taskId: String, ownerId: String)
}
