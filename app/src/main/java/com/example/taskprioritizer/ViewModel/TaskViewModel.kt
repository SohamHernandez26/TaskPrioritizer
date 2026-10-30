package com.example.taskprioritizer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskprioritizer.model.Task
import com.example.taskprioritizer.repository.TaskRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<TaskUiState>(TaskUiState.Loading)

    val uiState: StateFlow<TaskUiState> = _uiState

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError

    private var observeJob: Job? = null

    init {
        loadTasks()
    }

    fun loadTasks() {
        observeJob?.cancel()

        _uiState.value = TaskUiState.Loading

        observeJob = viewModelScope.launch {
            try {
                repository.observeTasks().collect { tasks ->
                    _uiState.value = if (tasks.isEmpty()) {
                        TaskUiState.Empty
                    } else {
                        TaskUiState.Success(tasks)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = TaskUiState.Error(
                    "No se pudieron cargar las tareas."
                )
            }
        }
    }

    fun saveTask(
        task: Task,
        onSuccess: () -> Unit
    ) {
        if (_isSaving.value) return

        _isSaving.value = true
        _actionError.value = null

        viewModelScope.launch {
            try {
                val result = if (task.id.isBlank()) {
                    repository.createTask(task)
                } else {
                    repository.updateTask(task)
                }

                result.fold(
                    onSuccess = { onSuccess() },
                    onFailure = {
                        _actionError.value =
                            "No se pudo guardar la tarea."
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _actionError.value =
                    "No se pudo guardar la tarea."
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun deleteTask(
        taskId: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = repository.deleteTask(taskId)
                result.fold(
                    onSuccess = { onSuccess() },
                    onFailure = {
                        _actionError.value =
                            "No se pudo eliminar la tarea."
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _actionError.value =
                    "No se pudo eliminar la tarea."
            }
        }
    }

    fun getTaskById(taskId: String): Task? {
        val current = _uiState.value
        return if (current is TaskUiState.Success) {
            current.tasks.find { it.id == taskId }
        } else {
            null
        }
    }
}