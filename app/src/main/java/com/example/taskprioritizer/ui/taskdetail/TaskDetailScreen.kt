package com.example.taskprioritizer.ui.taskdetail


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.taskprioritizer.data.MockData
import com.example.taskprioritizer.model.EstadoTarea
import com.example.taskprioritizer.model.Prioridad

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    tareaId: Int,
    onVolver: () -> Unit
) {
    // Buscamos la tarea en la lista mock por su id.
    // "find" recorre la lista y devuelve la primera que cumpla la condición, o null si no hay ninguna.
    val tarea = MockData.tareas.find { it.id == tareaId }

    // Controla si el popup-recordatorio está visible.
    // Arranca visible solo si la tarea lleva 3 días o más sin atenderse (F3 de tu acuerdo de requisitos).
    var mostrarRecordatorio by remember(tareaId) {
        mutableStateOf((tarea?.diasSinAtender ?: 0) >= 3)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Tarea") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (tarea == null) {
            // Caso borde: si por alguna razón el id no existe (ej. se eliminó), avisamos en vez de crashear.
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text("No se encontró la tarea.")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when (tarea.prioridad) {
                        Prioridad.ALTA -> MaterialTheme.colorScheme.errorContainer
                        Prioridad.MEDIA -> MaterialTheme.colorScheme.tertiaryContainer
                        Prioridad.BAJA -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = tarea.titulo,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Prioridad: ${tarea.prioridad.name}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Badge de "pendiente hace X días" — solo aparece si aplica.
            if (tarea.diasSinAtender > 0 && tarea.estado == EstadoTarea.PENDIENTE) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "Pendiente desde hace ${tarea.diasSinAtender} días",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Row {
                Text("Fecha de entrega: ", fontWeight = FontWeight.Medium)
                Text(tarea.fechaEntrega.toString())
            }

            Row {
                Text("Tiempo estimado: ", fontWeight = FontWeight.Medium)
                Text("${tarea.tiempoEstimadoMinutos} min")
            }

            if (tarea.notas.isNotBlank()) {
                Column {
                    Text("Notas", fontWeight = FontWeight.Medium)
                    Text(tarea.notas, style = MaterialTheme.typography.bodyMedium)
                }
            }

            // Botón que marca la tarea como completada, si aún no lo está.
            if (tarea.estado == EstadoTarea.PENDIENTE) {
                Button(
                    onClick = {
                        val index = MockData.tareas.indexOfFirst { it.id == tareaId }
                        if (index != -1) {
                            MockData.tareas[index] = tarea.copy(
                                estado = EstadoTarea.COMPLETADA,
                                diasSinAtender = 0
                            )
                        }
                        onVolver()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Marcar como Completada")
                }
            } else {
                Text(
                    text = "✓ Completada",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Popup-recordatorio de tu Figma: aparece encima de todo cuando mostrarRecordatorio es true.
        if (mostrarRecordatorio && tarea.estado == EstadoTarea.PENDIENTE) {
            AlertDialog(
                onDismissRequest = { mostrarRecordatorio = false },
                icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                title = { Text("¡No olvides esta tarea!") },
                text = {
                    Text("Tu tarea \"${tarea.titulo}\" lleva ${tarea.diasSinAtender} días pendiente.")
                },
                confirmButton = {
                    TextButton(onClick = { mostrarRecordatorio = false }) {
                        Text("Ir a la tarea")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarRecordatorio = false }) {
                        Text("Recordar más tarde")
                    }
                }
            )
        }
    }
}