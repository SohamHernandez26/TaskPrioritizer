package com.example.taskprioritizer.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.taskprioritizer.ui.addtask.AddTaskScreen
import com.example.taskprioritizer.ui.home.HomeScreen
import com.example.taskprioritizer.ui.plantoday.PlanTodayScreen
import com.example.taskprioritizer.ui.taskdetail.TaskDetailScreen


object Rutas {
    const val HOME = "home"
    const val AGREGAR_TAREA = "agregar_tarea"
    const val DETALLE_TAREA = "detalle_tarea/{tareaId}"
    const val PLAN_HOY = "plan_hoy"

    fun detalleTarea(tareaId: Int) = "detalle_tarea/$tareaId"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Rutas.HOME
    ) {
        composable(Rutas.HOME) {
            HomeScreen(
                onTareaClick = { id ->
                    navController.navigate(Rutas.detalleTarea(id))
                },
                onAgregarClick = {
                    navController.navigate(Rutas.AGREGAR_TAREA)
                },
                onVerPlanClick = {
                    navController.navigate(Rutas.PLAN_HOY)
                }
            )
        }


        composable(Rutas.AGREGAR_TAREA) {
            AddTaskScreen(
                onGuardar = { navController.popBackStack() },
                onCancelar = { navController.popBackStack() }
            )
        }


        composable(
            route = Rutas.DETALLE_TAREA,
            arguments = listOf(navArgument("tareaId") { type = NavType.IntType })
        ) { backStackEntry ->
            val tareaId = backStackEntry.arguments?.getInt("tareaId") ?: 0
            TaskDetailScreen(
                tareaId = tareaId,
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Rutas.PLAN_HOY) {
            PlanTodayScreen()
        }
    }
}

@Composable
private fun PantallaPendiente(nombre: String) {
    Text(
        text = "$nombre (pendiente de construir)",
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    )
}