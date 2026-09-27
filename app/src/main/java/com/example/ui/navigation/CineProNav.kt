package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.editor.EditorScreen
import com.example.ui.editor.EditorViewModel
import com.example.ui.home.HomeScreen

object CineProRoutes {
    const val HOME = "home"
    const val EDITOR = "editor/{projectId}"
    fun editor(projectId: String) = "editor/$projectId"
}

@Composable
fun CineProNavApp(
    editorViewModel: EditorViewModel = viewModel()
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = CineProRoutes.HOME
    ) {
        composable(CineProRoutes.HOME) {
            HomeScreen(
                editorViewModel = editorViewModel,
                onOpenProject = { projectId ->
                    navController.navigate(CineProRoutes.editor(projectId))
                }
            )
        }

        composable(
            route = CineProRoutes.EDITOR,
            arguments = listOf(
                navArgument("projectId") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            EditorScreen(
                viewModel = editorViewModel,
                projectId = projectId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
