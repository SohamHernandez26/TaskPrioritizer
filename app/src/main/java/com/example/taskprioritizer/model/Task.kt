package com.example.taskprioritizer.model

import java.time.LocalDate

enum class Priority { HIGH, MEDIUM, LOW }

enum class TaskStatus { PENDING, COMPLETED }

data class Task(
    val id: String,
    val title: String,
    val notes: String = "",
    val priority: Priority = Priority.MEDIUM,
    val estimatedMinutes: Int = 30,
    val dueDate: LocalDate = LocalDate.now(),
    val status: TaskStatus = TaskStatus.PENDING,
    val ownerId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class TaskSnapshot(
    val tasks: List<Task> = emptyList(),
    val isFromCache: Boolean = false,
    val hasPendingWrites: Boolean = false,
    val syncError: String? = null
)

enum class WriteOutcome { SYNCED, PENDING }
