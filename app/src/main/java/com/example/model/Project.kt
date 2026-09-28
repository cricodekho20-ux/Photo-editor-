package com.example.model

data class Project(
    val id: Long = 0,
    val title: String = "Untitled Project",
    val width: Int = 1080,
    val height: Int = 1080,
    val aspectRatioLabel: String = "1:1",
    val backgroundConfig: BackgroundConfig = BackgroundConfig(),
    val layers: List<CanvasLayer> = emptyList(),
    val drawStrokes: List<DrawStroke> = emptyList(),
    val thumbnailBase64: String? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)

data class ProjectTemplate(
    val id: String,
    val title: String,
    val category: String,
    val width: Int,
    val height: Int,
    val aspectRatioLabel: String,
    val backgroundConfig: BackgroundConfig,
    val layers: List<CanvasLayer>,
    val drawStrokes: List<DrawStroke> = emptyList(),
    val previewGradientColors: List<Long> = listOf(0xFF6366F1, 0xFF06B6D4)
)
