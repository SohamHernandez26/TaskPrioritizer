package com.example.taskprioritizer.repository.impl

import com.example.taskprioritizer.data.local.ReminderDao
import com.example.taskprioritizer.data.local.ReminderEntity
import com.example.taskprioritizer.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow

class RoomReminderRepository(
    private val dao: ReminderDao
) : ReminderRepository {

    override fun observeReminders(
        ownerId: String
    ): Flow<List<ReminderEntity>> =
        dao.observeReminders(ownerId)

    override suspend fun saveReminder(
        reminder: ReminderEntity
    ) {
        dao.insertReminder(reminder)
    }

    override suspend fun deleteReminder(
        id: Long,
        ownerId: String
    ) {
        dao.deleteReminder(id, ownerId)
    }
}