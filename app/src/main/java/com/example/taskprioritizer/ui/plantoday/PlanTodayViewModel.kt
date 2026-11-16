package com.example.taskprioritizer.ui.plantoday

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskprioritizer.model.Task
import com.example.taskprioritizer.model.TaskRules
import com.example.taskprioritizer.model.TaskSnapshot
import com.example.taskprioritizer.model.TaskStatus
import com.example.taskprioritizer.repository.TaskRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlanTodayUiState(
    val isLoading: Boolean = true,
    val snapshot: TaskSnapshot = TaskSnapshot(),
    val error: String? = null
) {
    val pendingTasks: List<Task>
        get() = TaskRules.orderedTasks(snapshot.tasks.filter { it.status == TaskStatus.PENDING })
    val estimatedMinutes: Int get() = pendingTasks.sumOf { it.estimatedMinutes }
    val completedPercentage: Int
        get() = if (snapshot.tasks.isEmpty()) 0 else {
            snapshot.tasks.count { it.status == TaskStatus.COMPLETED } * 100 / snapshot.tasks.size
        }
}

class PlanTodayViewModel(private val repository: TaskRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(PlanTodayUiState())
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
            repository.observeTasks().catch { exception ->
                timeout?.cancel()
                _uiState.update { it.copy(isLoading = false, error = exception.message ?: "No se pudo cargar el plan.") }
            }.collect { snapshot ->
                timeout?.cancel()
                _uiState.update { it.copy(isLoading = false, snapshot = snapshot, error = null) }
            }
        }
    }
}
