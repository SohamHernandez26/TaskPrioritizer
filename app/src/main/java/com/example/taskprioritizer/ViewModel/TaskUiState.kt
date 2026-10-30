package com.example.taskprioritizer.viewmodel

import com.example.taskprioritizer.model.Task

sealed interface TaskUiState {

    data object Loading : TaskUiState

    data object Empty : TaskUiState

    data class Success(
        val tasks: List<Task>
    ) : TaskUiState

    data class Error(
        val message: String
    ) : TaskUiState
}