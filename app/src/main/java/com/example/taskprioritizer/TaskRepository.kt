
package com.example.taskprioritizer.repository

import com.example.taskprioritizer.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun observeTasks(): Flow<List<Task>>

    suspend fun createTask(task: Task): Result<Unit>

    suspend fun updateTask(task: Task): Result<Unit>

    suspend fun deleteTask(taskId: String): Result<Unit>
}
