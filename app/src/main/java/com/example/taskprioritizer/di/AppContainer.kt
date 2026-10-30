package com.example.taskprioritizer.di

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

    private val auth by lazy {
        FirebaseAuth.getInstance()
    }

    private val firestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val database by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            TaskDatabase::class.java,
            "task_database"
        ).build()
    }

    val authRepository: AuthRepository by lazy {
        FirebaseAuthRepository(auth)
    }

    val taskRepository: TaskRepository by lazy {
        FirestoreTaskRepository(
            firestore = firestore,
            auth = auth
        )
    }

    val reminderRepository: ReminderRepository by lazy {
        RoomReminderRepository(
            database.reminderDao()
        )
    }
}
