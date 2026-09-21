package com.example.taskprioritizer.ui.plantoday


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.taskprioritizer.data.MockData
import com.example.taskprioritizer.model.EstadoTarea
import com.example.taskprioritizer.model.Prioridad
import com.example.taskprioritizer.model.Tarea

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanTodayScreen() {
    // Solo tomamos las tareas pendientes, ordenadas igual que en Home:
    // primero por prioridad, luego por fecha de entrega más próxima.
    val tareasPendientes = MockData.tareas
        .filter { it.estado == EstadoTarea.PENDIENTE }
        .sortedWith(compareBy({ it.prioridad.ordinal }, { it.fechaEntrega }))

    val totalTareas = MockData.tareas.size
    val completadas = MockData.tareas.count { it.estado == EstadoTarea.COMPLETADA }
    val porcentajeCompletado = if (totalTareas > 0) {
        (completadas * 100) / totalTareas
    } else 0
    val minutosTotales = tareasPendientes.sumOf { it.tiempoEstimadoMinutos }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Tu Plan para Hoy") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ResumenDelDia(
                    porcentaje = porcentajeCompletado,
                    cantidadPendientes = tareasPendientes.size,
                    minutosTotales = minutosTotales
                )
            }

            item {
                Text(
                    text = "Secuencia Recomendada",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            itemsIndexed(tareasPendientes) { index, tarea ->
                PasoDelPlan(numero = index + 1, tarea = tarea)
            }
        }
    }
}

@Composable
private fun ResumenDelDia(porcentaje: Int, cantidadPendientes: Int, minutosTotales: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "$porcentaje%",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$cantidadPendientes tareas pendientes · ${minutosTotales / 60}h ${minutosTotales % 60}m estimadas",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun PasoDelPlan(numero: Int, tarea: Tarea) {
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "$numero.",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 12.dp)
            )
            Column {
                Text(tarea.titulo, fontWeight = FontWeight.Medium)
                Text(
                    text = "${tarea.prioridad.name} · ${tarea.tiempoEstimadoMinutos} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}