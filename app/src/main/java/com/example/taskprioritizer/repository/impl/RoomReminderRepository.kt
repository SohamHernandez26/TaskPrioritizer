package com.example.taskprioritizer.repository.impl

import com.example.taskprioritizer.data.local.ReminderDao
import com.example.taskprioritizer.data.local.ReminderEntity
import com.example.taskprioritizer.model.Reminder
import com.example.taskprioritizer.repository.ReminderRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class RoomReminderRepository(
    private val dao: ReminderDao
) : ReminderRepository {

    override fun observeReminders(
        ownerId: String
    ): Flow<List<Reminder>> =
        dao.observeReminders(ownerId)
            .map { reminders ->
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
            .catch { exception ->
                if (exception is CancellationException) {
                    throw exception
                }

                if (exception is Exception) {
                    RepositoryErrorReporter.record(
                        "room.observeReminders",
                        exception
                    )
                }

                // El ViewModel también debe recibir el error
                // para mostrar el mensaje correspondiente.
                throw exception
            }

    override suspend fun saveReminder(reminder: Reminder) {
        runOperation("room.saveReminder") {
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
    }

    override suspend fun deleteReminder(
        id: Long,
        ownerId: String
    ) {
        runOperation("room.deleteReminder") {
            dao.deleteReminder(id, ownerId)
        }
    }

    override suspend fun deleteForTask(
        taskId: String,
        ownerId: String
    ) {
        runOperation("room.deleteForTask") {
            dao.deleteForTask(taskId, ownerId)
        }
    }

    private suspend fun runOperation(
        operation: String,
        action: suspend () -> Unit
    ) {
        try {
            action()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            RepositoryErrorReporter.record(operation, exception)
            throw exception
        }
    }
}