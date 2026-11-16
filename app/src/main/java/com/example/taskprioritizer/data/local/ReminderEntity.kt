package com.example.taskprioritizer.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: String,
    val ownerId: String,
    val scheduledAt: Long,
    val enabled: Boolean = true,
    @ColumnInfo(defaultValue = "''") val taskTitle: String = ""
)
