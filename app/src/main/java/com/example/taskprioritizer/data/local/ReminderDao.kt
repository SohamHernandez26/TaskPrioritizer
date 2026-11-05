package com.example.taskprioritizer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE ownerId = :ownerId AND enabled = 1 ORDER BY scheduledAt ASC")
    fun observeReminders(ownerId: String): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE id = :id AND ownerId = :ownerId")
    suspend fun deleteReminder(id: Long, ownerId: String)

    @Query("DELETE FROM reminders WHERE taskId = :taskId AND ownerId = :ownerId")
    suspend fun deleteForTask(taskId: String, ownerId: String)

    @Transaction
    suspend fun replaceReminder(reminder: ReminderEntity) {
        deleteForTask(reminder.taskId, reminder.ownerId)
        insertReminder(reminder)
    }
}
