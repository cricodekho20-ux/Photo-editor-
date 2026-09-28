package com.example.data

import com.example.model.AdminPost
import com.example.model.Project
import com.example.model.ProjectTemplate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class ProjectRepository(
    private val projectDao: ProjectDao,
    private val adminPostDao: AdminPostDao
) {

    val allProjects: Flow<List<Project>> = projectDao.getAllProjects().map { entities ->
        entities.map { it.toDomain() }
    }

    val allAdminPosts: Flow<List<AdminPost>> = adminPostDao.getAllPosts()

    suspend fun getProjectById(id: Long): Project? {
        return projectDao.getProjectById(id)?.toDomain()
    }

    suspend fun saveProject(project: Project): Long {
        val updatedProject = project.copy(updatedAt = System.currentTimeMillis())
        val entity = ProjectEntity.fromDomain(updatedProject)
        return projectDao.insertProject(entity)
    }

    suspend fun duplicateProject(project: Project): Long {
        val duplicated = project.copy(
            id = 0,
            title = "${project.title} (Copy)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return saveProject(duplicated)
    }

    suspend fun deleteProject(id: Long) {
        projectDao.deleteProjectById(id)
    }

    suspend fun createFromTemplate(template: ProjectTemplate): Project {
        val project = Project(
            id = 0,
            title = template.title,
            width = template.width,
            height = template.height,
            aspectRatioLabel = template.aspectRatioLabel,
            backgroundConfig = template.backgroundConfig,
            layers = template.layers.map { it.copy(id = UUID.randomUUID().toString()) },
            drawStrokes = template.drawStrokes.map { it.copy(id = UUID.randomUUID().toString()) },
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val id = saveProject(project)
        return project.copy(id = id)
    }

    // Admin Panel Methods
    suspend fun publishAdminPost(post: AdminPost): Long {
        return adminPostDao.insertPost(post)
    }

    suspend fun deleteAdminPost(id: Long) {
        adminPostDao.deletePostById(id)
    }
}
