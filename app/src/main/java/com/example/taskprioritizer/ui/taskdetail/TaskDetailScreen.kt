package com.example.taskprioritizer.ui.taskdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.taskprioritizer.model.TaskStatus
import com.example.taskprioritizer.ui.common.FeedbackCard
import com.example.taskprioritizer.ui.common.LoadingState
import com.example.taskprioritizer.ui.common.SyncNotice
import com.example.taskprioritizer.ui.common.displayName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    state: TaskDetailUiState,
    onComplete: () -> Unit,
    onScheduleReminder: () -> Unit,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier, topBar = {
        TopAppBar(title = { Text("Detalle de tarea") }, navigationIcon = {
            TextButton(onClick = onBack) { Text("Volver") }
        })
    }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SyncNotice(state.snapshot)
            if (state.isLoading || state.isSaving) LoadingState()
            state.error?.let { FeedbackCard(it, isError = true, onRetry = onRetry) }
            state.message?.let { FeedbackCard(it) }
            val task = state.task
            if (task != null) {
                Text(task.title, style = MaterialTheme.typography.headlineMedium)
                Text("Prioridad: ${task.priority.displayName()}")
                Text("Fecha de entrega: ${task.dueDate}")
                Text("Tiempo estimado: ${task.estimatedMinutes} minutos")
                if (task.notes.isNotBlank()) {
                    Text("Notas", style = MaterialTheme.typography.titleMedium)
                    Text(task.notes)
                }
                if (task.status == TaskStatus.PENDING) {
                    Button(onClick = onComplete, enabled = !state.isSaving, modifier = Modifier.fillMaxWidth()) {
                        Text("Marcar como completada")
                    }
                    OutlinedButton(onClick = onScheduleReminder, enabled = !state.isSaving, modifier = Modifier.fillMaxWidth()) {
                        Text("Recordarme en 30 minutos")
                    }
                    Text("El recordatorio se conserva en este teléfono y se muestra dentro de Mis tareas.", style = MaterialTheme.typography.bodySmall)
                } else {
                    Text("Actividad completada", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                }
            } else if (!state.isLoading && state.error == null) {
                FeedbackCard("La tarea no está disponible en los datos de este dispositivo. Vuelve a Mis tareas o reintenta con conexión.", onRetry = onRetry)
            }
        }
    }
}
