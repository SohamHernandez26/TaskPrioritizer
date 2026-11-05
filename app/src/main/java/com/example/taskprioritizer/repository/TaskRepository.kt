package com.example.taskprioritizer.repository

import com.example.taskprioritizer.model.Task
import com.example.taskprioritizer.model.TaskSnapshot
import com.example.taskprioritizer.model.WriteOutcome
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun observeTasks(): Flow<TaskSnapshot>
    suspend fun createTask(task: Task): Result<WriteOutcome>
    suspend fun updateTask(task: Task): Result<WriteOutcome>
    suspend fun deleteTask(taskId: String): Result<WriteOutcome>
}
