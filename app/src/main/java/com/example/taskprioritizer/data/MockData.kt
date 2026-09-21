package com.example.taskprioritizer.data

import androidx.compose.runtime.mutableStateListOf
import com.example.taskprioritizer.model.EstadoTarea
import com.example.taskprioritizer.model.Prioridad
import com.example.taskprioritizer.model.Tarea
import java.time.LocalDate

object MockData {

    val tareas = mutableStateListOf(
        Tarea(
            id = 1,
            titulo = "Proyecto de Física",
            notas = "Completar las gráficas de cinemática, revisar las soluciones de termodinámica y dar formato a la página de bibliografía antes de entregar.",
            prioridad = Prioridad.ALTA,
            tiempoEstimadoMinutos = 90,
            fechaEntrega = LocalDate.now(),
            estado = EstadoTarea.PENDIENTE,
            diasSinAtender = 3
        ),
        Tarea(
            id = 2,
            titulo = "Parcial de Cálculo",
            prioridad = Prioridad.ALTA,
            tiempoEstimadoMinutos = 120,
            fechaEntrega = LocalDate.now().plusDays(2),
            estado = EstadoTarea.PENDIENTE
        ),
        Tarea(
            id = 3,
            titulo = "Presentación de Comunicación",
            prioridad = Prioridad.MEDIA,
            tiempoEstimadoMinutos = 45,
            fechaEntrega = LocalDate.now().plusDays(1),
            estado = EstadoTarea.PENDIENTE
        ),
        Tarea(
            id = 4,
            titulo = "Lectura Asignada",
            prioridad = Prioridad.BAJA,
            tiempoEstimadoMinutos = 30,
            fechaEntrega = LocalDate.now().plusDays(3),
            estado = EstadoTarea.PENDIENTE
        )
    )
}