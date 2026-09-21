package com.example.taskprioritizer.model


import java.time.LocalDate

enum class Prioridad {
    ALTA, MEDIA, BAJA
}

enum class EstadoTarea {
    PENDIENTE, COMPLETADA
}

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