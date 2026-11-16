package com.example.taskprioritizer.model

import java.time.LocalDate

object TaskRules {
    const val MAX_TITLE_LENGTH = 50
    const val MAX_NOTES_LENGTH = 500
    const val MAX_ESTIMATED_MINUTES = 1440

    fun titleError(title: String): String? = when {
        title.trim().length < 3 -> "Escribe al menos 3 caracteres."
        title.trim().length > MAX_TITLE_LENGTH -> "Usa como máximo 50 caracteres."
        else -> null
    }

    fun minutesError(minutes: String): String? {
        val value = minutes.toIntOrNull()
        return if (value == null || value !in 1..MAX_ESTIMATED_MINUTES) {
            "Ingresa un número entero entre 1 y 1440 minutos."
        } else {
            null
        }
    }

    fun dateError(date: String, today: LocalDate = LocalDate.now()): String? {
        val parsed = if (Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}").matches(date)) {
            runCatching { LocalDate.parse(date) }.getOrNull()
        } else {
            null
        }
        return when {
            parsed == null -> "Usa el formato AAAA-MM-DD o el calendario."
            parsed.isBefore(today) -> "La fecha no puede ser anterior a hoy."
            else -> null
        }
    }

    fun notesError(notes: String): String? =
        if (notes.length > MAX_NOTES_LENGTH) "Usa como máximo 500 caracteres." else null

    fun orderedTasks(tasks: List<Task>): List<Task> = tasks.sortedWith(
        compareBy<Task> { it.status.ordinal }
            .thenBy { it.priority.ordinal }
            .thenBy { it.dueDate }
            .thenBy { it.title.lowercase() }
    )
}
