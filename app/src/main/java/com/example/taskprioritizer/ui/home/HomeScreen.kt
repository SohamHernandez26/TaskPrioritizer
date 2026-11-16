package com.example.taskprioritizer.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.taskprioritizer.model.Reminder
import com.example.taskprioritizer.ui.common.FeedbackCard
import com.example.taskprioritizer.ui.common.LoadingState
import com.example.taskprioritizer.ui.common.ReminderCard
import com.example.taskprioritizer.ui.common.SyncNotice
import com.example.taskprioritizer.ui.common.TaskCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeUiState,
    savedMessage: String,
    onDismissMessage: () -> Unit,
    onTaskClick: (String) -> Unit,
    onAddClick: () -> Unit,
    onPlanClick: () -> Unit,
    onSignOut: () -> Unit,
    onRetry: () -> Unit,
    onDeleteReminder: (Reminder) -> Unit,
    onSnoozeReminder: (Reminder) -> Unit,
    onDismissReminder: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text("Mis tareas académicas") }, actions = {
                TextButton(onClick = onPlanClick) { Text("Plan") }
                TextButton(onClick = onSignOut) { Text("Salir") }
            })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) { Text("+ Agregar", Modifier.padding(horizontal = 16.dp)) }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (savedMessage.isNotEmpty()) {
                item(key = "saved-message") {
                    FeedbackCard(savedMessage)
                    TextButton(onClick = onDismissMessage) { Text("Cerrar aviso") }
                }
            }
            item(key = "sync-status") { SyncNotice(state.snapshot) }
            if (state.isLoading) item(key = "loading") { LoadingState() }
            state.error?.let { message -> item(key = "error") { FeedbackCard(message, isError = true, onRetry = onRetry) } }
            if (!state.isLoading && state.error == null && state.tasks.isEmpty()) {
                item(key = "empty") {
                    FeedbackCard(if (state.snapshot.isFromCache) "No hay tareas guardadas en este dispositivo. Puedes agregar una o reconectarte." else "Aún no tienes tareas. Usa + Agregar para crear la primera.")
                }
            }
            items(state.tasks, key = { "task-${it.id}" }) { task ->
                TaskCard(task, onClick = { onTaskClick(task.id) })
            }
            item(key = "reminders-title") {
                Text("Recordatorios en este dispositivo", style = MaterialTheme.typography.titleMedium)
                Text("Se muestran aquí y te avisan al abrir Mis tareas cuando llegue la hora.", style = MaterialTheme.typography.bodySmall)
            }
            state.reminderError?.let { message ->
                item(key = "reminder-error") { FeedbackCard(message, isError = true, onRetry = onRetry) }
            }
            if (state.reminders.isEmpty()) item(key = "empty-reminders") { Text("Puedes crear un recordatorio desde el detalle de una tarea.") }
            items(state.reminders, key = { "reminder-${it.id}" }) { reminder ->
                ReminderCard(
                    reminder = reminder,
                    onOpen = { onTaskClick(reminder.taskId) },
                    onDelete = { onDeleteReminder(reminder) },
                    enabled = !state.isUpdatingReminder
                )
            }
        }
    }
    state.dueReminder?.let { reminder ->
        AlertDialog(
            onDismissRequest = onDismissReminder,
            title = { Text("Tienes una tarea pendiente") },
            text = { Text(reminder.taskTitle.ifBlank { "Revisa tu actividad pendiente." }) },
            confirmButton = {
                TextButton(onClick = {
                    onDismissReminder()
                    onTaskClick(reminder.taskId)
                }) { Text("Ver tarea") }
            },
            dismissButton = {
                TextButton(onClick = { onSnoozeReminder(reminder) }, enabled = !state.isUpdatingReminder) { Text("Recordar en 30 min") }
            }
        )
    }
}
