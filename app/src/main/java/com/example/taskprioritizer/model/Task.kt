package com.example.taskprioritizer.model

import java.time.LocalDate

enum class Prioridad {
    ALTA, MEDIA, BAJA
}

enum class EstadoTarea {
    PENDIENTE, COMPLETADA
}

// Modelo nuevo: integración con Firestore.
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

// Modelo anterior: conservar temporalmente para las pantallas existentes.
data class Tarea(
    val id: Int,
    val titulo: String,
    val notas: String = "",
    val prioridad: Prioridad,
    val tiempoEstimadoMinutos: Int,
    val fechaEntrega: LocalDate,
    val estado: EstadoTarea = EstadoTarea.PENDIENTE,
    val diasSinAtender: Int = 0
)
