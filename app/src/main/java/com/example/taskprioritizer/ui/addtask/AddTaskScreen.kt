package com.example.taskprioritizer.ui.addtask

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.taskprioritizer.data.MockData
import com.example.taskprioritizer.model.EstadoTarea
import com.example.taskprioritizer.model.Prioridad
import com.example.taskprioritizer.model.Tarea
import java.time.LocalDate

// Opciones de tiempo estimado: lo que se ve en pantalla y los minutos reales que guardamos.
private val opcionesTiempo = listOf(
    "10m" to 10,
    "30m" to 30,
    "1h" to 60,
    "1h 30m" to 90,
    "2h" to 120
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    onGuardar: () -> Unit,
    onCancelar: () -> Unit
) {

    var nombre by remember { mutableStateOf("") }
    var prioridadSeleccionada by remember { mutableStateOf<Prioridad?>(null) }
    var tiempoSeleccionado by remember { mutableStateOf<Int?>(null) }

    //
    val nombreError = when {
        nombre.isBlank() -> null
        nombre.trim().length < 3 -> "Mínimo 3 caracteres"
        nombre.trim().length > 50 -> "Máximo 50 caracteres"
        else -> null
    }

    // El formulario es válido solo si hay nombre de al menos 3 caracteres,
       val formularioValido = nombre.trim().length >= 3 &&
            nombreError == null &&
            prioridadSeleccionada != null &&
            tiempoSeleccionado != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar Actividad") },
                navigationIcon = {
                    IconButton(onClick = onCancelar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Campo 1: Nombre
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre de la actividad") },
                placeholder = { Text("ej. Proyecto de Física") },
                isError = nombreError != null,
                supportingText = {
                    if (nombreError != null) Text(nombreError)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Campo 2: Prioridad
            Column {
                Text("Nivel de prioridad", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Prioridad.values().forEach { opcion ->
                        FilterChip(
                            selected = prioridadSeleccionada == opcion,
                            onClick = { prioridadSeleccionada = opcion },
                            label = { Text(opcion.name) }
                        )
                    }
                }
            }

            // Campo 3: Tiempo estimado
            Column {
                Text("Tiempo estimado", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    opcionesTiempo.forEach { (etiqueta, minutos) ->
                        FilterChip(
                            selected = tiempoSeleccionado == minutos,
                            onClick = { tiempoSeleccionado = minutos },
                            label = { Text(etiqueta) }
                        )
                    }
                }
            }

            // Fecha de entrega: por ahora fija en "hoy". Lo dejamos como texto simple;
            // más adelante se puede reemplazar por un selector de fecha real.
            Text(
                text = "Fecha de entrega: Hoy (${LocalDate.now()})",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Botón condicionado: solo se habilita si el formulario es válido.
            Button(
                onClick = {
                    val nuevoId = (MockData.tareas.maxOfOrNull { it.id } ?: 0) + 1
                    MockData.tareas.add(
                        Tarea(
                            id = nuevoId,
                            titulo = nombre.trim(),
                            prioridad = prioridadSeleccionada!!,
                            tiempoEstimadoMinutos = tiempoSeleccionado!!,
                            fechaEntrega = LocalDate.now(),
                            estado = EstadoTarea.PENDIENTE
                        )
                    )
                    onGuardar()
                },
                enabled = formularioValido,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Guardar Actividad")
            }
        }
    }
}