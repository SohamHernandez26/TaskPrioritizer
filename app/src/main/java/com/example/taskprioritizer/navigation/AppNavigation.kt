package com.example.taskprioritizer.navigation

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.taskprioritizer.di.AppContainer
import com.example.taskprioritizer.ui.addtask.AddTaskScreen
import com.example.taskprioritizer.ui.addtask.AddTaskViewModel
import com.example.taskprioritizer.ui.auth.AuthScreen
import com.example.taskprioritizer.ui.auth.AuthViewModel
import com.example.taskprioritizer.ui.common.LoadingState
import com.example.taskprioritizer.ui.common.SimpleViewModelFactory
import com.example.taskprioritizer.ui.home.HomeScreen
import com.example.taskprioritizer.ui.home.HomeViewModel
import com.example.taskprioritizer.ui.plantoday.PlanTodayScreen
import com.example.taskprioritizer.ui.plantoday.PlanTodayViewModel
import com.example.taskprioritizer.ui.taskdetail.TaskDetailScreen
import com.example.taskprioritizer.ui.taskdetail.TaskDetailViewModel

private object Routes {
    const val HOME = "home"
    const val ADD_TASK = "add-task"
    const val PLAN = "plan"
    const val DETAIL = "task/{taskId}"
    fun task(id: String): String = "task/${Uri.encode(id)}"
}

@Composable
fun AppNavigation(container: AppContainer, modifier: Modifier = Modifier) {
    val authViewModel: AuthViewModel = viewModel(factory = SimpleViewModelFactory { AuthViewModel(container.authRepository) })
    val session by authViewModel.uiState.collectAsStateWithLifecycle()
    Surface(modifier = modifier.fillMaxSize()) {
        val ownerId = session.userId
        when {
            session.checkingSession -> LoadingState(Modifier.safeDrawingPadding())
            ownerId == null -> AuthScreen(
                state = session,
                onEmailChange = authViewModel::onEmailChange,
                onPasswordChange = authViewModel::onPasswordChange,
                onConfirmationChange = authViewModel::onConfirmationChange,
                onSubmit = authViewModel::onSubmit,
                onModeChange = authViewModel::onModeChange
            )
            else -> key(ownerId) {
                UserNavigation(container, ownerId, authViewModel::onSignOut)
            }
        }
    }
}

@Composable
private fun UserNavigation(
    container: AppContainer,
    ownerId: String,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.HOME, modifier = modifier) {
        composable(Routes.HOME) { entry ->
            val homeViewModel: HomeViewModel = viewModel(
                factory = SimpleViewModelFactory { HomeViewModel(container.taskRepository, container.reminderRepository, ownerId) }
            )
            val state by homeViewModel.uiState.collectAsStateWithLifecycle()
            val message by entry.savedStateHandle.getStateFlow("savedMessage", "").collectAsStateWithLifecycle()
            HomeScreen(
                state = state,
                savedMessage = message,
                onDismissMessage = { entry.savedStateHandle["savedMessage"] = "" },
                onTaskClick = { navController.navigate(Routes.task(it)) },
                onAddClick = { navController.navigate(Routes.ADD_TASK) },
                onPlanClick = { navController.navigate(Routes.PLAN) },
                onSignOut = onSignOut,
                onRetry = homeViewModel::onRetry,
                onDeleteReminder = homeViewModel::onDeleteReminder,
                onSnoozeReminder = homeViewModel::onSnoozeReminder,
                onDismissReminder = homeViewModel::onDismissReminder
            )
        }
        composable(Routes.ADD_TASK) {
            val addViewModel: AddTaskViewModel = viewModel(factory = SimpleViewModelFactory { AddTaskViewModel(container.taskRepository) })
            val state by addViewModel.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(state.saved) {
                if (state.saved) {
                    navController.previousBackStackEntry?.savedStateHandle?.set("savedMessage", state.savedMessage.orEmpty())
                    navController.popBackStack()
                }
            }
            AddTaskScreen(
                state = state,
                onTitleChange = addViewModel::onTitleChange,
                onNotesChange = addViewModel::onNotesChange,
                onPriorityChange = addViewModel::onPriorityChange,
                onMinutesChange = addViewModel::onMinutesChange,
                onDateChange = addViewModel::onDateChange,
                onSave = addViewModel::onSave,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.PLAN) {
            val planViewModel: PlanTodayViewModel = viewModel(factory = SimpleViewModelFactory { PlanTodayViewModel(container.taskRepository) })
            val state by planViewModel.uiState.collectAsStateWithLifecycle()
            PlanTodayScreen(state, { navController.navigate(Routes.task(it)) }, { navController.popBackStack() }, planViewModel::onRetry)
        }
        composable(Routes.DETAIL, arguments = listOf(navArgument("taskId") { type = NavType.StringType })) { entry ->
            val taskId = entry.arguments?.getString("taskId").orEmpty()
            val detailViewModel: TaskDetailViewModel = viewModel(factory = SimpleViewModelFactory {
                TaskDetailViewModel(container.taskRepository, container.reminderRepository, ownerId, taskId)
            })
            val state by detailViewModel.uiState.collectAsStateWithLifecycle()
            TaskDetailScreen(
                state = state,
                onComplete = detailViewModel::onComplete,
                onScheduleReminder = detailViewModel::onScheduleReminder,
                onBack = { navController.popBackStack() },
                onRetry = detailViewModel::onRetry
            )
        }
    }
}
