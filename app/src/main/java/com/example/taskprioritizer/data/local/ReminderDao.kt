package com.example.taskprioritizer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Query(
        "SELECT * FROM reminders " +
                "WHERE ownerId = :ownerId " +
                "ORDER BY scheduledAt ASC"
    )
    fun observeReminders(
        ownerId: String
    ): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(
        reminder: ReminderEntity
    )

    @Query(
        "DELETE FROM reminders " +
                "WHERE id = :id AND ownerId = :ownerId"
    )
    suspend fun deleteReminder(
        id: Long,
        ownerId: String
    )
}