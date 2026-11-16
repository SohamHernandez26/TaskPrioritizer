package com.example.taskprioritizer.ui.plantoday

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.taskprioritizer.ui.common.FeedbackCard
import com.example.taskprioritizer.ui.common.LoadingState
import com.example.taskprioritizer.ui.common.SyncNotice
import com.example.taskprioritizer.ui.common.TaskCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanTodayScreen(
    state: PlanTodayUiState,
    onTaskClick: (String) -> Unit,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier, topBar = {
        TopAppBar(title = { Text("Tu plan para hoy") }, navigationIcon = {
            TextButton(onClick = onBack) { Text("Volver") }
        })
    }) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "sync-status") { SyncNotice(state.snapshot) }
            if (state.isLoading) item(key = "loading") { LoadingState() }
            state.error?.let { message -> item(key = "error") { FeedbackCard(message, isError = true, onRetry = onRetry) } }
            if (!state.isLoading) {
                item(key = "summary") {
                    Text("${state.completedPercentage}% de tareas completadas", style = MaterialTheme.typography.titleLarge)
                    Text("${state.pendingTasks.size} pendientes · ${state.estimatedMinutes} minutos estimados")
                    Text("Orden sugerido por prioridad y fecha de entrega.")
                }
                if (state.pendingTasks.isEmpty() && state.error == null) {
                    item(key = "empty") { FeedbackCard("No hay tareas pendientes en los datos disponibles.") }
                }
            }
            itemsIndexed(state.pendingTasks, key = { _, task -> task.id }) { index, task ->
                Text("${index + 1}. Siguiente actividad", style = MaterialTheme.typography.labelLarge)
                TaskCard(task, onClick = { onTaskClick(task.id) })
            }
        }
    }
}
