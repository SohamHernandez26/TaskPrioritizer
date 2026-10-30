package com.example.taskprioritizer

import android.app.Application
import com.example.taskprioritizer.di.AppContainer

class TaskPrioritizerApplication : Application() {

    val container: AppContainer by lazy {
        AppContainer(this)
    }
}
