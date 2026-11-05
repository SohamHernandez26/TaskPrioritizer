package com.example.taskprioritizer.repository.impl

import com.example.taskprioritizer.data.local.ReminderDao
import com.example.taskprioritizer.data.local.ReminderEntity
import com.example.taskprioritizer.model.Reminder
import com.example.taskprioritizer.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomReminderRepository(private val dao: ReminderDao) : ReminderRepository {
    override fun observeReminders(ownerId: String): Flow<List<Reminder>> =
        dao.observeReminders(ownerId).map { reminders ->
            reminders.map { entity ->
                Reminder(
                    id = entity.id,
                    taskId = entity.taskId,
                    ownerId = entity.ownerId,
                    taskTitle = entity.taskTitle,
                    scheduledAt = entity.scheduledAt,
                    enabled = entity.enabled
                )
            }
        }

    override suspend fun saveReminder(reminder: Reminder) {
        dao.replaceReminder(
            ReminderEntity(
                id = reminder.id,
                taskId = reminder.taskId,
                ownerId = reminder.ownerId,
                scheduledAt = reminder.scheduledAt,
                enabled = reminder.enabled,
                taskTitle = reminder.taskTitle
            )
        )
    }

    override suspend fun deleteReminder(id: Long, ownerId: String) {
        dao.deleteReminder(id, ownerId)
    }

    override suspend fun deleteForTask(taskId: String, ownerId: String) {
        dao.deleteForTask(taskId, ownerId)
    }
}
