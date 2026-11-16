package com.example.taskprioritizer.ui.addtask

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.taskprioritizer.model.Priority
import com.example.taskprioritizer.ui.common.FeedbackCard
import com.example.taskprioritizer.ui.common.LoadingState
import com.example.taskprioritizer.ui.common.displayName
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    state: AddTaskUiState,
    onTitleChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onPriorityChange: (Priority) -> Unit,
    onMinutesChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCalendar by rememberSaveable { mutableStateOf(false) }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text("Agregar actividad") }, navigationIcon = {
                TextButton(onClick = onBack, enabled = !state.isSaving) { Text("Volver") }
            })
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = onTitleChange,
                label = { Text("Nombre de la actividad") },
                isError = state.titleTouched && state.titleError != null,
                supportingText = { if (state.titleTouched) state.titleError?.let { Text(it) } },
                enabled = !state.isSaving,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Column {
                Text("Prioridad", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Priority.entries.forEach { priority ->
                        FilterChip(
                            selected = state.priority == priority,
                            onClick = { onPriorityChange(priority) },
                            label = { Text(priority.displayName()) },
                            enabled = !state.isSaving
                        )
                    }
                }
                if (state.priority == null) Text("Selecciona una prioridad.", color = MaterialTheme.colorScheme.error)
            }
            OutlinedTextField(
                value = state.estimatedMinutes,
                onValueChange = onMinutesChange,
                label = { Text("Tiempo estimado en minutos") },
                isError = state.minutesTouched && state.minutesError != null,
                supportingText = { if (state.minutesTouched) state.minutesError?.let { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = !state.isSaving,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.dueDate,
                onValueChange = onDateChange,
                label = { Text("Fecha de entrega (AAAA-MM-DD)") },
                isError = state.dateError != null,
                supportingText = { state.dateError?.let { Text(it) } },
                trailingIcon = { TextButton(onClick = { showCalendar = true }, enabled = !state.isSaving) { Text("Elegir") } },
                enabled = !state.isSaving,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.notes,
                onValueChange = onNotesChange,
                label = { Text("Notas (opcional)") },
                isError = state.notesError != null,
                supportingText = { Text(state.notesError ?: "${state.notes.length}/500") },
                enabled = !state.isSaving,
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            state.error?.let { FeedbackCard(it, isError = true) }
            if (state.isSaving) LoadingState()
            Button(onClick = onSave, enabled = state.isValid && !state.isSaving && !state.saved, modifier = Modifier.fillMaxWidth()) {
                Text("Guardar actividad")
            }
        }
    }
    if (showCalendar) {
        val initialDate = runCatching { LocalDate.parse(state.dueDate) }.getOrNull()
            ?.takeIf { it.year in 1900..2100 } ?: LocalDate.now()
        val picker = rememberDatePickerState(initialSelectedDateMillis = initialDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { showCalendar = false },
            confirmButton = {
                TextButton(onClick = {
                    picker.selectedDateMillis?.let { millis ->
                        onDateChange(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString())
                    }
                    showCalendar = false
                }, enabled = picker.selectedDateMillis != null) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showCalendar = false }) { Text("Cancelar") } }
        ) { DatePicker(state = picker) }
    }
}
