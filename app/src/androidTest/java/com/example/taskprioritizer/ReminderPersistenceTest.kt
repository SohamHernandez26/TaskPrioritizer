package com.example.taskprioritizer

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.taskprioritizer.data.local.TaskDatabase
import com.example.taskprioritizer.model.Reminder
import com.example.taskprioritizer.repository.impl.RoomReminderRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReminderPersistenceTest {
    private lateinit var context: Context
    private lateinit var database: TaskDatabase
    private val databaseName = "reminder-persistence-test.db"

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(databaseName)
        database = openDatabase()
    }

    @After
    fun cleanup() {
        database.close()
        context.deleteDatabase(databaseName)
    }

    @Test
    fun reminderSurvivesDatabaseReopenAndIsScopedToOwner() = runBlocking {
        val repository = RoomReminderRepository(database.reminderDao())
        repository.saveReminder(Reminder(taskId = "task", ownerId = "owner-a", taskTitle = "Estudiar cálculo", scheduledAt = 12345))
        database.close()
        database = openDatabase()
        val reopened = RoomReminderRepository(database.reminderDao())
        assertEquals("Estudiar cálculo", reopened.observeReminders("owner-a").first().single().taskTitle)
        assertEquals(emptyList<Reminder>(), reopened.observeReminders("owner-b").first())
    }

    @Test
    fun reschedulingDoesNotDuplicateTheReminder() = runBlocking {
        val repository = RoomReminderRepository(database.reminderDao())
        val reminder = Reminder(taskId = "task", ownerId = "owner-a", taskTitle = "Lectura", scheduledAt = 100)
        repository.saveReminder(reminder)
        repository.saveReminder(reminder.copy(scheduledAt = 200))
        val saved = repository.observeReminders("owner-a").first()
        assertEquals(1, saved.size)
        assertEquals(200L, saved.single().scheduledAt)
    }

    private fun openDatabase(): TaskDatabase = Room.databaseBuilder(context, TaskDatabase::class.java, databaseName)
        .addMigrations(TaskDatabase.migration1To2).build()
}
