package com.example.taskprioritizer.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SimpleViewModelFactory(private val createViewModel: () -> ViewModel) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val instance = createViewModel()
        require(modelClass.isInstance(instance))
        @Suppress("UNCHECKED_CAST")
        return instance as T
    }
}
