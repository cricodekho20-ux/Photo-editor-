package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.PixCraftDatabase
import com.example.data.ProjectRepository
import com.example.model.Project
import com.example.ui.editor.EditorScreen
import com.example.ui.editor.EditorViewModel
import com.example.ui.home.HomeScreen
import com.example.ui.home.HomeViewModel
import com.example.ui.theme.PixCraftTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = PixCraftDatabase.getDatabase(applicationContext)
        val repository = ProjectRepository(database.projectDao(), database.adminPostDao())

        setContent {
            PixCraftTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0F1117)
                ) {
                    PixCraftApp(repository = repository)
                }
            }
        }
    }
}

@Composable
fun PixCraftApp(
    repository: ProjectRepository,
    modifier: Modifier = Modifier
) {
    var currentEditingProject by remember { mutableStateOf<Project?>(null) }

    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.provideFactory(repository)
    )

    AnimatedContent(
        targetState = currentEditingProject,
        transitionSpec = {
            if (targetState != null) {
                slideInHorizontally { it } + fadeIn() togetherWith
                        slideOutHorizontally { -it } + fadeOut()
            } else {
                slideInHorizontally { -it } + fadeIn() togetherWith
                        slideOutHorizontally { it } + fadeOut()
            }
        },
        label = "AppScreenTransition"
    ) { activeProject ->
        if (activeProject != null) {
            // Key the editor viewModel to the project ID and title so when a new project opens, fresh state loads
            val editorViewModel: EditorViewModel = viewModel(
                key = "editor_${activeProject.id}_${activeProject.title}_${activeProject.createdAt}",
                factory = EditorViewModel.provideFactory(repository, activeProject)
            )

            EditorScreen(
                viewModel = editorViewModel,
                onNavigateBack = {
                    currentEditingProject = null
                },
                modifier = modifier
            )
        } else {
            HomeScreen(
                viewModel = homeViewModel,
                onOpenProject = { project ->
                    currentEditingProject = project
                },
                modifier = modifier
            )
        }
    }
}
