package com.example.taskprioritizer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: String,
    val ownerId: String,
    val scheduledAt: Long,
    val enabled: Boolean = true
)