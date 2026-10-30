package com.example.taskprioritizer.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.taskprioritizer.viewmodel.AuthViewModel
import com.example.taskprioritizer.viewmodel.TaskViewModel

class AppViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras
    ): T {
        return when {
            modelClass.isAssignableFrom(TaskViewModel::class.java) ->
                TaskViewModel(container.taskRepository) as T

            modelClass.isAssignableFrom(AuthViewModel::class.java) ->
                AuthViewModel(container.authRepository) as T

            else -> throw IllegalArgumentException(
                "ViewModel desconocido: ${modelClass.name}"
            )
        }
    }
}
}