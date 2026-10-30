
package com.example.taskprioritizer.model

import java.time.LocalDate

data class Task(
    val id: String,
    val title: String,
    val notes: String = "",
    val priority: Prioridad = Prioridad.MEDIA,
    val estimatedMinutes: Int = 30,
    val dueDate: LocalDate = LocalDate.now(),
    val status: EstadoTarea = EstadoTarea.PENDIENTE,
    val ownerId: String = ""
)
