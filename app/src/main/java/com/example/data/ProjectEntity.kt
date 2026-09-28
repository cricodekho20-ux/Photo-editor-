package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.BackgroundConfig
import com.example.model.CanvasLayer
import com.example.model.DrawStroke
import com.example.model.Project

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val width: Int,
    val height: Int,
    val aspectRatioLabel: String,
    val backgroundConfig: BackgroundConfig,
    val layers: List<CanvasLayer>,
    val drawStrokes: List<DrawStroke>,
    val thumbnailBase64: String?,
    val updatedAt: Long,
    val createdAt: Long
) {
    fun toDomain(): Project = Project(
        id = id,
        title = title,
        width = width,
        height = height,
        aspectRatioLabel = aspectRatioLabel,
        backgroundConfig = backgroundConfig,
        layers = layers,
        drawStrokes = drawStrokes,
        thumbnailBase64 = thumbnailBase64,
        updatedAt = updatedAt,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(project: Project): ProjectEntity = ProjectEntity(
            id = project.id,
            title = project.title,
            width = project.width,
            height = project.height,
            aspectRatioLabel = project.aspectRatioLabel,
            backgroundConfig = project.backgroundConfig,
            layers = project.layers,
            drawStrokes = project.drawStrokes,
            thumbnailBase64 = project.thumbnailBase64,
            updatedAt = project.updatedAt,
            createdAt = project.createdAt
        )
    }
}
