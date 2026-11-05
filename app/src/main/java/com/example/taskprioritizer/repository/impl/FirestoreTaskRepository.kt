package com.example.taskprioritizer.repository.impl

import com.example.taskprioritizer.model.Priority
import com.example.taskprioritizer.model.Task
import com.example.taskprioritizer.model.TaskRules
import com.example.taskprioritizer.model.TaskSnapshot
import com.example.taskprioritizer.model.TaskStatus
import com.example.taskprioritizer.model.WriteOutcome
import com.example.taskprioritizer.repository.TaskRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.MetadataChanges
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import java.time.LocalDate

class FirestoreTaskRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : TaskRepository {
    private val writeErrors = MutableStateFlow<Pair<String, String>?>(null)

    private fun currentOwner(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("Inicia sesión nuevamente.")

    private fun tasks(ownerId: String) =
        firestore.collection("users").document(ownerId).collection("tasks")

    override fun observeTasks(): Flow<TaskSnapshot> {
        val ownerId = auth.currentUser?.uid
        val snapshots = callbackFlow<TaskSnapshot> {
            if (ownerId == null) {
                close(IllegalStateException("Inicia sesión nuevamente."))
                return@callbackFlow
            }
            val listener = tasks(ownerId).addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null) {
                    close(IllegalStateException(errorMessage(error), error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    try {
                        trySend(
                            TaskSnapshot(
                                tasks = snapshot.documents.map { it.toDomain(ownerId) },
                                isFromCache = snapshot.metadata.isFromCache,
                                hasPendingWrites = snapshot.metadata.hasPendingWrites()
                            )
                        )
                    } catch (exception: Exception) {
                        close(IllegalStateException("Una tarea tiene datos inválidos. Revisa sus campos en Firebase.", exception))
                    }
                }
            }
            awaitClose { listener.remove() }
        }
        return combine(snapshots, writeErrors) { snapshot, failure ->
            snapshot.copy(syncError = failure?.takeIf { it.first == ownerId }?.second)
        }
    }

    override suspend fun createTask(task: Task): Result<WriteOutcome> = write { ownerId ->
        require(task.id.isNotBlank()) { "No se pudo identificar la tarea." }
        validate(task)
        tasks(ownerId).document(task.id).set(task.toFields(ownerId))
    }

    override suspend fun updateTask(task: Task): Result<WriteOutcome> = write { ownerId ->
        require(task.id.isNotBlank()) { "No se pudo identificar la tarea." }
        tasks(ownerId).document(task.id).update(task.toFields(ownerId))
    }

    override suspend fun deleteTask(taskId: String): Result<WriteOutcome> = write { ownerId ->
        tasks(ownerId).document(taskId).delete()
    }

    private suspend fun write(
        operation: (String) -> com.google.android.gms.tasks.Task<Void>
    ): Result<WriteOutcome> {
        return try {
            val ownerId = currentOwner()
            writeErrors.value = null
            val pendingWrite = operation(ownerId)
            pendingWrite.addOnFailureListener { exception ->
                writeErrors.value = ownerId to errorMessage(exception)
            }
            // Firestore keeps the write queued when this bounded wait ends.
            val outcome = withTimeoutOrNull(WRITE_WAIT_MILLIS) {
                pendingWrite.await()
                WriteOutcome.SYNCED
            } ?: WriteOutcome.PENDING
            Result.success(outcome)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(IllegalStateException(errorMessage(exception), exception))
        }
    }

    private fun validate(task: Task) {
        require(TaskRules.titleError(task.title) == null) { "Revisa el nombre de la tarea." }
        require(TaskRules.minutesError(task.estimatedMinutes.toString()) == null) { "Revisa el tiempo estimado." }
        require(TaskRules.dateError(task.dueDate.toString()) == null) { "Revisa la fecha de entrega." }
        require(TaskRules.notesError(task.notes) == null) { "Revisa las notas." }
    }

    private fun DocumentSnapshot.toDomain(ownerId: String): Task = Task(
        id = id,
        title = getString("title").orEmpty(),
        notes = getString("notes").orEmpty(),
        priority = when (getString("priority")) {
            "HIGH", "ALTA" -> Priority.HIGH
            "LOW", "BAJA" -> Priority.LOW
            else -> Priority.MEDIUM
        },
        estimatedMinutes = (getLong("estimatedMinutes") ?: 30L).toInt(),
        dueDate = LocalDate.parse(getString("dueDate") ?: LocalDate.now().toString()),
        status = when (getString("status")) {
            "COMPLETED", "COMPLETADA" -> TaskStatus.COMPLETED
            else -> TaskStatus.PENDING
        },
        ownerId = ownerId,
        createdAt = getLong("createdAt") ?: 0L
    )

    private fun Task.toFields(ownerId: String): Map<String, Any> = mapOf(
        "title" to title.trim(),
        "notes" to notes,
        "priority" to priority.name,
        "estimatedMinutes" to estimatedMinutes,
        "dueDate" to dueDate.toString(),
        "status" to status.name,
        "ownerId" to ownerId,
        "createdAt" to createdAt
    )

    private fun errorMessage(exception: Exception): String = when (exception) {
        is FirebaseFirestoreException -> when (exception.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                "Firebase rechazó el cambio. Revisa los permisos y las reglas de Firestore."
            FirebaseFirestoreException.Code.UNAUTHENTICATED -> "Tu sesión expiró. Vuelve a iniciar sesión."
            FirebaseFirestoreException.Code.UNAVAILABLE -> "No se pudo contactar al servidor. Revisa tu conexión."
            else -> "No se pudo sincronizar con Firebase. Intenta nuevamente."
        }
        is IllegalArgumentException -> exception.message ?: "Revisa los datos de la tarea."
        is IllegalStateException -> exception.message ?: "Inicia sesión nuevamente."
        else -> "No se pudo guardar el cambio. Intenta nuevamente."
    }

    private companion object {
        const val WRITE_WAIT_MILLIS = 4000L
    }
}
