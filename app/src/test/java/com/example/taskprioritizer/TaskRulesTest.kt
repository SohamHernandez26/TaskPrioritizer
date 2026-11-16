package com.example.taskprioritizer

import com.example.taskprioritizer.model.Priority
import com.example.taskprioritizer.model.Task
import com.example.taskprioritizer.model.TaskRules
import com.example.taskprioritizer.model.TaskStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class TaskRulesTest {
    @Test
    fun titleRequiresThreeToFiftyTrimmedCharacters() {
        assertNotNull(TaskRules.titleError("   "))
        assertNotNull(TaskRules.titleError("ab"))
        assertNull(TaskRules.titleError("  Estudiar física  "))
        assertNotNull(TaskRules.titleError("x".repeat(51)))
    }

    @Test
    fun estimatedTimeRejectsZeroNegativeFractionsAndOverflow() {
        listOf("", "0", "-5", "1.5", "1441", "999999999999").forEach {
            assertNotNull(TaskRules.minutesError(it))
        }
        assertNull(TaskRules.minutesError("1"))
        assertNull(TaskRules.minutesError("1440"))
    }

    @Test
    fun datesRejectPastAndInvalidCalendarDays() {
        val today = LocalDate.of(2026, 10, 29)
        assertNotNull(TaskRules.dateError("2026-02-30", today))
        assertNotNull(TaskRules.dateError("29/10/2026", today))
        assertNotNull(TaskRules.dateError("2026-10-28", today))
        assertNull(TaskRules.dateError("2026-10-29", today))
        assertNull(TaskRules.dateError("2026-11-01", today))
    }

    @Test
    fun prioritizationKeepsPendingHighPriorityTasksFirst() {
        val today = LocalDate.of(2026, 10, 29)
        val low = Task("low", "Lectura", priority = Priority.LOW, dueDate = today)
        val highLater = Task("later", "Proyecto", priority = Priority.HIGH, dueDate = today.plusDays(2))
        val highToday = Task("today", "Examen", priority = Priority.HIGH, dueDate = today)
        val completed = highToday.copy(id = "done", status = TaskStatus.COMPLETED)
        assertEquals(listOf("today", "later", "low", "done"), TaskRules.orderedTasks(listOf(completed, low, highLater, highToday)).map { it.id })
    }
}
