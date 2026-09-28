package com.example.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.DefaultTemplates
import com.example.data.ProjectRepository
import com.example.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class HomeTab {
    MY_PROJECTS,
    TEMPLATES,
    SETTINGS
}

data class HomeUiState(
    val projects: List<Project> = emptyList(),
    val templates: List<ProjectTemplate> = DefaultTemplates.getTemplates(),
    val adminPosts: List<AdminPost> = emptyList(),
    val currentTab: HomeTab = HomeTab.MY_PROJECTS,
    val showNewProjectSheet: Boolean = false,
    val showAdminPinDialog: Boolean = false,
    val isAdminScreenOpen: Boolean = false,
    val projectToRename: Project? = null,
    val toastMessage: String? = null
)

class HomeViewModel(
    private val repository: ProjectRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.allProjects.collect { projectList ->
                _uiState.update { it.copy(projects = projectList) }
            }
        }
        viewModelScope.launch {
            repository.allAdminPosts.collect { posts ->
                _uiState.update { it.copy(adminPosts = posts) }
            }
        }
    }

    fun setTab(tab: HomeTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun setShowNewProjectSheet(show: Boolean) {
        _uiState.update { it.copy(showNewProjectSheet = show) }
    }

    fun setShowAdminPinDialog(show: Boolean) {
        _uiState.update { it.copy(showAdminPinDialog = show) }
    }

    fun setAdminScreenOpen(open: Boolean) {
        _uiState.update { it.copy(isAdminScreenOpen = open, showAdminPinDialog = false) }
    }

    fun setProjectToRename(project: Project?) {
        _uiState.update { it.copy(projectToRename = project) }
    }

    fun renameProject(project: Project, newTitle: String) {
        if (newTitle.isNotBlank()) {
            viewModelScope.launch {
                repository.saveProject(project.copy(title = newTitle.trim()))
                _uiState.update { it.copy(projectToRename = null, toastMessage = "Project renamed") }
            }
        }
    }

    fun duplicateProject(project: Project) {
        viewModelScope.launch {
            repository.duplicateProject(project)
            _uiState.update { it.copy(toastMessage = "Project duplicated") }
        }
    }

    fun deleteProject(projectId: Long) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
            _uiState.update { it.copy(toastMessage = "Project deleted") }
        }
    }

    fun createNewProject(
        title: String = "Untitled Artwork",
        width: Int = 1080,
        height: Int = 1080,
        aspectRatioLabel: String = "1:1",
        backgroundConfig: BackgroundConfig = BackgroundConfig(),
        layers: List<CanvasLayer> = emptyList(),
        onCreated: (Project) -> Unit
    ) {
        viewModelScope.launch {
            val project = Project(
                id = 0,
                title = title,
                width = width,
                height = height,
                aspectRatioLabel = aspectRatioLabel,
                backgroundConfig = backgroundConfig,
                layers = layers,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            val id = repository.saveProject(project)
            val created = project.copy(id = id)
            _uiState.update { it.copy(showNewProjectSheet = false) }
            onCreated(created)
        }
    }

    fun createFromTemplate(
        template: ProjectTemplate,
        onCreated: (Project) -> Unit
    ) {
        viewModelScope.launch {
            val project = repository.createFromTemplate(template)
            onCreated(project)
        }
    }

    // Admin Operations
    fun publishAdminPost(
        title: String,
        description: String,
        category: String,
        badge: String,
        bannerText: String?
    ) {
        viewModelScope.launch {
            val post = AdminPost(
                title = title,
                description = description,
                category = category,
                badge = badge,
                bannerText = bannerText,
                createdAt = System.currentTimeMillis()
            )
            repository.publishAdminPost(post)
            _uiState.update { it.copy(toastMessage = "Post published successfully!") }
        }
    }

    fun deleteAdminPost(id: Long) {
        viewModelScope.launch {
            repository.deleteAdminPost(id)
            _uiState.update { it.copy(toastMessage = "Admin post removed") }
        }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    companion object {
        fun provideFactory(repository: ProjectRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(repository) as T
                }
            }
    }
}
