package com.example.taskprioritizer.di

import android.content.Context
import androidx.room.Room
import com.example.taskprioritizer.data.local.TaskDatabase
import com.example.taskprioritizer.repository.AuthRepository
import com.example.taskprioritizer.repository.ReminderRepository
import com.example.taskprioritizer.repository.TaskRepository
import com.example.taskprioritizer.repository.impl.FirebaseAuthRepository
import com.example.taskprioritizer.repository.impl.FirestoreTaskRepository
import com.example.taskprioritizer.repository.impl.RoomReminderRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AppContainer(context: Context) {
    private val auth = FirebaseAuth.getInstance()
    private val database = Room.databaseBuilder(
        context.applicationContext,
        TaskDatabase::class.java,
        "task_database"
    ).addMigrations(TaskDatabase.migration1To2).build()

    val authRepository: AuthRepository = FirebaseAuthRepository(auth)
    val taskRepository: TaskRepository = FirestoreTaskRepository(FirebaseFirestore.getInstance(), auth)
    val reminderRepository: ReminderRepository = RoomReminderRepository(database.reminderDao())
}
