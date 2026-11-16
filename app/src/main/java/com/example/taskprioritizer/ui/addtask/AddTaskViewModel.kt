package com.example.taskprioritizer.ui.addtask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskprioritizer.model.Priority
import com.example.taskprioritizer.model.Task
import com.example.taskprioritizer.model.TaskRules
import com.example.taskprioritizer.model.WriteOutcome
import com.example.taskprioritizer.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

data class AddTaskUiState(
    val title: String = "",
    val notes: String = "",
    val priority: Priority? = null,
    val estimatedMinutes: String = "",
    val dueDate: String = LocalDate.now().toString(),
    val titleTouched: Boolean = false,
    val minutesTouched: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
    val savedMessage: String? = null
) {
    val titleError: String? get() = TaskRules.titleError(title)
    val minutesError: String? get() = TaskRules.minutesError(estimatedMinutes)
    val dateError: String? get() = TaskRules.dateError(dueDate)
    val notesError: String? get() = TaskRules.notesError(notes)
    val isValid: Boolean
        get() = titleError == null && minutesError == null && dateError == null && notesError == null && priority != null
}

class AddTaskViewModel(private val repository: TaskRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AddTaskUiState())
    val uiState = _uiState.asStateFlow()
    private val taskId = UUID.randomUUID().toString()

    fun onTitleChange(value: String) { _uiState.update { it.copy(title = value, titleTouched = true, error = null) } }
    fun onNotesChange(value: String) { _uiState.update { it.copy(notes = value, error = null) } }
    fun onPriorityChange(value: Priority) { _uiState.update { it.copy(priority = value, error = null) } }
    fun onMinutesChange(value: String) { _uiState.update { it.copy(estimatedMinutes = value, minutesTouched = true, error = null) } }
    fun onDateChange(value: String) { _uiState.update { it.copy(dueDate = value, error = null) } }

    fun onSave() {
        val state = _uiState.value
        if (!state.isValid || state.isSaving || state.saved) return
        val priority = state.priority ?: return
        val minutes = state.estimatedMinutes.toIntOrNull() ?: return
        _uiState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            val result = repository.createTask(
                Task(
                    id = taskId,
                    title = state.title.trim(),
                    notes = state.notes.trim(),
                    priority = priority,
                    estimatedMinutes = minutes,
                    dueDate = LocalDate.parse(state.dueDate)
                )
            )
            result.fold(
                onSuccess = { outcome ->
                    val message = if (outcome == WriteOutcome.PENDING) {
                        "Tarea guardada localmente. Pendiente de confirmación del servidor; se enviará al recuperar conexión."
                    } else {
                        "Tarea guardada en Firebase."
                    }
                    _uiState.update { it.copy(isSaving = false, saved = true, savedMessage = message) }
                },
                onFailure = { exception ->
                    _uiState.update { it.copy(isSaving = false, error = exception.message ?: "No se pudo guardar la tarea.") }
                }
            )
        }
    }
}
