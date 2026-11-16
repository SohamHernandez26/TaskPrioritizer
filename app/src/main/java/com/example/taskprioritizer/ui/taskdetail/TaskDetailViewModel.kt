package com.example.taskprioritizer.ui.taskdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskprioritizer.model.Reminder
import com.example.taskprioritizer.model.Task
import com.example.taskprioritizer.model.TaskSnapshot
import com.example.taskprioritizer.model.TaskStatus
import com.example.taskprioritizer.model.WriteOutcome
import com.example.taskprioritizer.repository.ReminderRepository
import com.example.taskprioritizer.repository.TaskRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TaskDetailUiState(
    val isLoading: Boolean = true,
    val task: Task? = null,
    val snapshot: TaskSnapshot = TaskSnapshot(),
    val isSaving: Boolean = false,
    val error: String? = null,
    val message: String? = null
)

class TaskDetailViewModel(
    private val taskRepository: TaskRepository,
    private val reminderRepository: ReminderRepository,
    private val ownerId: String,
    private val taskId: String
) : ViewModel() {
    private val _uiState = MutableStateFlow(TaskDetailUiState())
    val uiState = _uiState.asStateFlow()
    private var observation: Job? = null
    private var timeout: Job? = null

    init { onRetry() }

    fun onRetry() {
        observation?.cancel()
        timeout?.cancel()
        _uiState.update { it.copy(isLoading = true, error = null) }
        timeout = viewModelScope.launch {
            delay(10000)
            _uiState.update {
                if (it.isLoading) it.copy(isLoading = false, error = "La lectura tardó demasiado. Puedes reintentar.") else it
            }
        }
        observation = viewModelScope.launch {
            taskRepository.observeTasks().catch { exception ->
                timeout?.cancel()
                _uiState.update { it.copy(isLoading = false, error = exception.message ?: "No se pudo leer la tarea.") }
            }.collect { snapshot ->
                timeout?.cancel()
                _uiState.update {
                    it.copy(isLoading = false, task = snapshot.tasks.find { task -> task.id == taskId }, snapshot = snapshot)
                }
            }
        }
    }

    fun onComplete() {
        val task = _uiState.value.task ?: return
        if (_uiState.value.isSaving || task.status == TaskStatus.COMPLETED) return
        _uiState.update { it.copy(isSaving = true, error = null, message = null) }
        viewModelScope.launch {
            val result = taskRepository.updateTask(task.copy(status = TaskStatus.COMPLETED))
            result.fold(
                onSuccess = { outcome ->
                    val message = if (outcome == WriteOutcome.PENDING) {
                        "Cambio guardado localmente y pendiente de confirmación del servidor."
                    } else {
                        "Tarea completada y sincronizada."
                    }
                    _uiState.update { it.copy(message = message) }
                    try {
                        reminderRepository.deleteForTask(taskId, ownerId)
                    } catch (exception: CancellationException) {
                        throw exception
                    } catch (exception: Exception) {
                        _uiState.update { it.copy(error = "La tarea se actualizó, pero debes eliminar su recordatorio desde Mis tareas.") }
                    }
                },
                onFailure = { exception ->
                    _uiState.update { it.copy(error = exception.message ?: "No se pudo completar la tarea.") }
                }
            )
            _uiState.update { it.copy(isSaving = false) }
        }
    }

    fun onScheduleReminder() {
        val task = _uiState.value.task ?: return
        if (_uiState.value.isSaving || task.status == TaskStatus.COMPLETED) return
        _uiState.update { it.copy(isSaving = true, error = null, message = null) }
        viewModelScope.launch {
            try {
                reminderRepository.saveReminder(
                    Reminder(
                        taskId = taskId,
                        ownerId = ownerId,
                        taskTitle = task.title,
                        scheduledAt = System.currentTimeMillis() + 30 * 60 * 1000L
                    )
                )
                _uiState.update { it.copy(message = "Recordatorio guardado para dentro de 30 minutos. Lo verás en Mis tareas.") }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update { it.copy(error = "No se pudo guardar el recordatorio local.") }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }
}
