package com.example.taskprioritizer.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskprioritizer.model.Reminder
import com.example.taskprioritizer.model.Task
import com.example.taskprioritizer.model.TaskRules
import com.example.taskprioritizer.model.TaskSnapshot
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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val snapshot: TaskSnapshot = TaskSnapshot(),
    val reminders: List<Reminder> = emptyList(),
    val error: String? = null,
    val reminderError: String? = null,
    val dueReminder: Reminder? = null,
    val isUpdatingReminder: Boolean = false
) {
    val tasks: List<Task> get() = TaskRules.orderedTasks(snapshot.tasks)
}

class HomeViewModel(
    private val taskRepository: TaskRepository,
    private val reminderRepository: ReminderRepository,
    private val ownerId: String
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()
    private var taskJob: Job? = null
    private var reminderJob: Job? = null
    private var timeoutJob: Job? = null
    private val dismissedReminderIds = mutableSetOf<Long>()

    init {
        onRetry()
        viewModelScope.launch {
            while (isActive) {
                updateDueReminder()
                delay(30000)
            }
        }
    }

    fun onRetry() {
        taskJob?.cancel()
        reminderJob?.cancel()
        timeoutJob?.cancel()
        _uiState.update { it.copy(isLoading = true, error = null, reminderError = null) }
        timeoutJob = viewModelScope.launch {
            delay(10000)
            _uiState.update {
                if (it.isLoading) it.copy(isLoading = false, error = "La lectura tardó demasiado. Revisa la conexión y reintenta.") else it
            }
        }
        taskJob = viewModelScope.launch {
            taskRepository.observeTasks().catch { exception ->
                timeoutJob?.cancel()
                _uiState.update { it.copy(isLoading = false, error = exception.message ?: "No se pudieron cargar las tareas.") }
            }.collect { snapshot ->
                timeoutJob?.cancel()
                _uiState.update { it.copy(isLoading = false, snapshot = snapshot, error = null) }
            }
        }
        reminderJob = viewModelScope.launch {
            reminderRepository.observeReminders(ownerId).catch {
                _uiState.update { it.copy(reminderError = "No se pudieron leer los recordatorios locales.") }
            }.collect { reminders ->
                _uiState.update { it.copy(reminders = reminders) }
                updateDueReminder()
            }
        }
    }

    private fun updateDueReminder() {
        val reminder = _uiState.value.reminders.firstOrNull {
            it.scheduledAt <= System.currentTimeMillis() && it.id !in dismissedReminderIds
        }
        _uiState.update { it.copy(dueReminder = reminder) }
    }

    fun onDismissReminder() {
        _uiState.value.dueReminder?.let { dismissedReminderIds.add(it.id) }
        _uiState.update { it.copy(dueReminder = null) }
    }

    fun onSnoozeReminder(reminder: Reminder) = updateReminder {
        reminderRepository.saveReminder(reminder.copy(scheduledAt = System.currentTimeMillis() + 30 * 60 * 1000L))
    }

    fun onDeleteReminder(reminder: Reminder) = updateReminder {
        reminderRepository.deleteReminder(reminder.id, ownerId)
    }

    private fun updateReminder(action: suspend () -> Unit) {
        if (_uiState.value.isUpdatingReminder) return
        _uiState.update { it.copy(isUpdatingReminder = true, reminderError = null) }
        viewModelScope.launch {
            try {
                action()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update { it.copy(reminderError = "No se pudo modificar el recordatorio local.") }
            } finally {
                _uiState.update { it.copy(isUpdatingReminder = false) }
            }
        }
    }
}
