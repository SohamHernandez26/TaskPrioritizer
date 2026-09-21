package com.example.taskprioritizer.ui.home

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.taskprioritizer.data.MockData
import com.example.taskprioritizer.model.Prioridad
import com.example.taskprioritizer.model.Tarea

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onTareaClick: (Int) -> Unit,
    onAgregarClick: () -> Unit,
    onVerPlanClick: () -> Unit
) {

    val tareasOrdenadas = MockData.tareas.sortedWith(
        compareBy(
            { it.prioridad.ordinal },
            { it.fechaEntrega }
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Tareas") },
                actions = {
                    TextButton(onClick = onVerPlanClick) {
                        Text("Ver Plan")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAgregarClick) {
                Icon(Icons.Default.Add, contentDescription = "Agregar tarea")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tareasOrdenadas) { tarea ->
                TareaCard(
                    tarea = tarea,
                    onClick = { onTareaClick(tarea.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareaCard(tarea: Tarea, onClick: () -> Unit) {
    Card(
        onClick = onClick,
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
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        when (tarea.prioridad) {
                            Prioridad.ALTA -> MaterialTheme.colorScheme.error
                            Prioridad.MEDIA -> MaterialTheme.colorScheme.tertiary
                            Prioridad.BAJA -> MaterialTheme.colorScheme.secondary
                        }
                    )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(text = tarea.titulo, fontWeight = FontWeight.Medium)
                Text(
                    text = "${tarea.tiempoEstimadoMinutos} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}