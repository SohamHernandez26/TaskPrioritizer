package com.example.taskprioritizer.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.taskprioritizer.model.Priority
import com.example.taskprioritizer.model.Reminder
import com.example.taskprioritizer.model.Task
import com.example.taskprioritizer.model.TaskSnapshot
import com.example.taskprioritizer.model.TaskStatus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Priority.displayName(): String = when (this) {
    Priority.HIGH -> "Alta"
    Priority.MEDIUM -> "Media"
    Priority.LOW -> "Baja"
}

@Composable
fun FeedbackCard(
    message: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    onRetry: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(message)
            if (onRetry != null) TextButton(onClick = onRetry) { Text("Reintentar") }
        }
    }
}

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(Modifier.size(24.dp))
        Text("Cargando…")
    }
}

@Composable
fun SyncNotice(snapshot: TaskSnapshot, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        snapshot.syncError?.let { FeedbackCard(it, isError = true) }
        if (snapshot.hasPendingWrites) {
            FeedbackCard("Hay cambios guardados localmente pendientes de confirmación. Se enviarán cuando haya conexión y permiso del servidor.")
        } else if (snapshot.isFromCache) {
            FeedbackCard("Mostrando datos guardados en este dispositivo. La conexión con el servidor aún no está confirmada.")
        }
    }
}

@Composable
fun TaskCard(task: Task, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(task.title, style = MaterialTheme.typography.titleMedium)
            Text("Prioridad ${task.priority.displayName()} · ${task.estimatedMinutes} min")
            Text("Entrega: ${task.dueDate}", style = MaterialTheme.typography.bodySmall)
            Text(
                if (task.status == TaskStatus.COMPLETED) "Completada" else "Pendiente",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ReminderCard(
    reminder: Reminder,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val date = Instant.ofEpochMilli(reminder.scheduledAt).atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(reminder.taskTitle.ifBlank { "Tarea guardada" }, style = MaterialTheme.typography.titleSmall)
            Text("Recordar: $date")
            Row {
                TextButton(onClick = onOpen, enabled = enabled) { Text("Ver tarea") }
                TextButton(onClick = onDelete, enabled = enabled) { Text("Eliminar recordatorio") }
            }
        }
    }
}
