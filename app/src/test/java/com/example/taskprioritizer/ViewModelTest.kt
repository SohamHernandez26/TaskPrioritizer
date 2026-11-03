package com.example.taskprioritizer

import androidx.lifecycle.viewModelScope
import com.example.taskprioritizer.model.Priority
import com.example.taskprioritizer.model.Reminder
import com.example.taskprioritizer.model.Task
import com.example.taskprioritizer.model.TaskSnapshot
import com.example.taskprioritizer.model.WriteOutcome
import com.example.taskprioritizer.repository.ReminderRepository
import com.example.taskprioritizer.repository.TaskRepository
import com.example.taskprioritizer.ui.addtask.AddTaskViewModel
import com.example.taskprioritizer.ui.home.HomeViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun invalidFormNeverWrites() = runTest {
        val repository = FakeTaskRepository()
        val viewModel = AddTaskViewModel(repository)
        viewModel.onSave()
        assertEquals(0, repository.writeCount)
        assertFalse(viewModel.uiState.value.isSaving)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun offlineQueuedWriteFinishesAndUsesStableId() = runTest {
        val repository = FakeTaskRepository().apply { outcome = Result.success(WriteOutcome.PENDING) }
        val viewModel = AddTaskViewModel(repository)
        fillValidForm(viewModel)
        viewModel.onSave()
        runCurrent()
        assertTrue(viewModel.uiState.value.saved)
        assertFalse(viewModel.uiState.value.isSaving)
        assertTrue(viewModel.uiState.value.savedMessage.orEmpty().contains("Pendiente"))
        viewModel.onSave()
        assertEquals(1, repository.writeCount)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun failedWriteKeepsFormAndCanRetrySameDocument() = runTest {
        val repository = FakeTaskRepository().apply { outcome = Result.failure(IllegalStateException("Permiso denegado")) }
        val viewModel = AddTaskViewModel(repository)
        fillValidForm(viewModel)
        viewModel.onSave()
        runCurrent()
        val firstId = repository.lastTask?.id
        assertFalse(viewModel.uiState.value.saved)
        assertFalse(viewModel.uiState.value.isSaving)
        assertEquals("Estudiar cálculo", viewModel.uiState.value.title)
        assertNotNull(viewModel.uiState.value.error)
        repository.outcome = Result.success(WriteOutcome.SYNCED)
        viewModel.onSave()
        runCurrent()
        assertEquals(firstId, repository.lastTask?.id)
        assertTrue(viewModel.uiState.value.saved)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun silentDataSourceStopsLoadingAfterDeadline() = runTest {
        val repository = FakeTaskRepository().apply { snapshots = flow { awaitCancellation() } }
        val viewModel = HomeViewModel(repository, FakeReminderRepository(), "owner")
        advanceTimeBy(10001)
        runCurrent()
        assertFalse(viewModel.uiState.value.isLoading)
        assertNotNull(viewModel.uiState.value.error)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun cachedEmptyListAndLocalReminderRemainVisible() = runTest {
        val repository = FakeTaskRepository().apply { snapshots = flowOf(TaskSnapshot(isFromCache = true)) }
        val reminders = FakeReminderRepository().apply {
            values.value = listOf(Reminder(taskId = "task", ownerId = "owner", taskTitle = "Recordatorio local", scheduledAt = Long.MAX_VALUE))
        }
        val viewModel = HomeViewModel(repository, reminders, "owner")
        runCurrent()
        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.tasks.isEmpty())
        assertTrue(viewModel.uiState.value.snapshot.isFromCache)
        assertEquals("Recordatorio local", viewModel.uiState.value.reminders.single().taskTitle)
        viewModel.viewModelScope.cancel()
    }

    private fun fillValidForm(viewModel: AddTaskViewModel) {
        viewModel.onTitleChange("Estudiar cálculo")
        viewModel.onPriorityChange(Priority.HIGH)
        viewModel.onMinutesChange("45")
    }
}

private class FakeTaskRepository : TaskRepository {
    var snapshots: Flow<TaskSnapshot> = flowOf(TaskSnapshot())
    var outcome: Result<WriteOutcome> = Result.success(WriteOutcome.SYNCED)
    var writeCount: Int = 0
    var lastTask: Task? = null
    override fun observeTasks(): Flow<TaskSnapshot> = snapshots
    override suspend fun createTask(task: Task): Result<WriteOutcome> {
        writeCount += 1
        lastTask = task
        return outcome
    }
    override suspend fun updateTask(task: Task): Result<WriteOutcome> = outcome
    override suspend fun deleteTask(taskId: String): Result<WriteOutcome> = outcome
}

private class FakeReminderRepository : ReminderRepository {
    val values = MutableStateFlow<List<Reminder>>(emptyList())
    override fun observeReminders(ownerId: String): Flow<List<Reminder>> = values
    override suspend fun saveReminder(reminder: Reminder) { values.value = listOf(reminder) }
    override suspend fun deleteReminder(id: Long, ownerId: String) { values.value = values.value.filterNot { it.id == id } }
    override suspend fun deleteForTask(taskId: String, ownerId: String) { values.value = values.value.filterNot { it.taskId == taskId } }
}
