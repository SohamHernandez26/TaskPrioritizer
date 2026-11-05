package com.example.taskprioritizer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.taskprioritizer.navigation.AppNavigation
import com.example.taskprioritizer.ui.theme.TaskPrioritizerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as TaskPrioritizerApplication).container
        setContent {
            TaskPrioritizerTheme {
                AppNavigation(container)
            }
        }
    }
}
