package com.example.taskprioritizer.model

data class Reminder(
    val id: Long = 0,
    val taskId: String,
    val ownerId: String,
    val taskTitle: String,
    val scheduledAt: Long,
    val enabled: Boolean = true
)
