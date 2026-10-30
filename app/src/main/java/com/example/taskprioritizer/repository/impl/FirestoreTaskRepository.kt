
package com.example.taskprioritizer.repository.impl

import com.example.taskprioritizer.model.Task
import com.example.taskprioritizer.model.Prioridad
import com.example.taskprioritizer.model.EstadoTarea
import com.example.taskprioritizer.repository.TaskRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

class FirestoreTaskRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : TaskRepository {

    private fun tasks(uid: String) =
        firestore.collection("users")
            .document(uid)
            .collection("tasks")

    private fun currentUid(): String =
        requireNotNull(auth.currentUser?.uid) {
            "Debes iniciar sesión para realizar esta operación"
        }

    override fun observeTasks(): Flow<List<Task>> = callbackFlow {
        val user = auth.currentUser

        if (user == null) {
            close(IllegalStateException("Inicia sesión"))
            return@callbackFlow
        }

        val listener = tasks(user.uid)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val result = runCatching {
                    snapshot?.documents.orEmpty().map { doc ->

                        Task(
                            id = doc.id,
                            title = doc.getString("title").orEmpty(),
                            notes = doc.getString("notes").orEmpty(),
                            priority = Prioridad.valueOf(
                                doc.getString("priority") ?: "MEDIA"
                            ),
                            estimatedMinutes = (
                                    doc.getLong("estimatedMinutes") ?: 30L
                                    ).toInt(),
                            dueDate = LocalDate.parse(
                                doc.getString("dueDate")
                                    ?: LocalDate.now().toString()
                            ),
                            status = EstadoTarea.valueOf(
                                doc.getString("status") ?: "PENDIENTE"
                            ),
                            ownerId = doc.getString("ownerId").orEmpty()
                        )
                    }
                }

                result
                    .onSuccess { trySend(it) }
                    .onFailure { close(it) }
            }

        awaitClose {
            listener.remove()
        }
    }

    override suspend fun createTask(task: Task): Result<Unit> =
        runCatching {
            val uid = currentUid()
            val document = tasks(uid).document()

            document.set(task.toMap(uid)).await()
            Unit
        }

    override suspend fun updateTask(task: Task): Result<Unit> =
        runCatching {
            val uid = currentUid()

            tasks(uid)
                .document(task.id)
                .update(task.toMap(uid))
                .await()

            Unit
        }

    override suspend fun deleteTask(taskId: String): Result<Unit> =
        runCatching {
            val uid = currentUid()

            tasks(uid)
                .document(taskId)
                .delete()
                .await()

            Unit
        }

    private fun Task.toMap(uid: String) = mapOf(
        "title" to title,
        "notes" to notes,
        "priority" to priority.name,
        "estimatedMinutes" to estimatedMinutes,
        "dueDate" to dueDate.toString(),
        "status" to status.name,
        "ownerId" to uid
    )
}
