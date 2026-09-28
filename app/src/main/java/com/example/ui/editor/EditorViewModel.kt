package com.example.ui.editor

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ProjectRepository
import com.example.model.*
import com.example.util.CanvasExporter
import com.example.util.ExportOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.UUID

enum class ActiveEditorTool {
    NONE,
    TEXT,
    IMAGE,
    ENHANCE,
    SHAPES,
    STICKERS,
    DRAW,
    BACKGROUND,
    LAYERS
}

data class EditorUiState(
    val project: Project = Project(),
    val selectedLayerId: String? = null,
    val activeTool: ActiveEditorTool = ActiveEditorTool.NONE,
    val isDrawModeActive: Boolean = false,
    val currentDrawTool: DrawToolType = DrawToolType.PEN,
    val currentDrawColor: Long = 0xFF6366F1,
    val currentBrushSizePercent: Float = 0.015f,
    val currentBrushOpacity: Float = 1.0f,
    val currentDrawingPoints: List<PointF2> = emptyList(),
    val canvasZoom: Float = 1.0f,
    val canvasPanX: Float = 0f,
    val canvasPanY: Float = 0f,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val isSaving: Boolean = false,
    val isExporting: Boolean = false,
    val toastMessage: String? = null,
    val showExportDialog: Boolean = false,
    val showRenameDialog: Boolean = false,
    val showQualityEnhanceDialog: Boolean = false
)

private data class HistorySnapshot(
    val layers: List<CanvasLayer>,
    val drawStrokes: List<DrawStroke>,
    val backgroundConfig: BackgroundConfig
)

class EditorViewModel(
    private val repository: ProjectRepository,
    initialProject: Project
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState(project = initialProject))
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val undoStack = mutableListOf<HistorySnapshot>()
    private val redoStack = mutableListOf<HistorySnapshot>()

    init {
        // Save initial snapshot
        pushHistorySnapshot(isInitial = true)
    }

    val selectedLayer: CanvasLayer?
        get() {
            val state = _uiState.value
            return state.project.layers.find { it.id == state.selectedLayerId }
        }

    private fun pushHistorySnapshot(isInitial: Boolean = false) {
        val current = _uiState.value.project
        val snapshot = HistorySnapshot(
            layers = current.layers,
            drawStrokes = current.drawStrokes,
            backgroundConfig = current.backgroundConfig
        )
        if (!isInitial) {
            undoStack.add(snapshot)
            if (undoStack.size > 30) {
                undoStack.removeAt(0)
            }
            redoStack.clear()
        }
        updateUndoRedoAvailability()
    }

    private fun updateUndoRedoAvailability() {
        _uiState.update {
            it.copy(
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val current = _uiState.value.project
        val currentSnapshot = HistorySnapshot(
            layers = current.layers,
            drawStrokes = current.drawStrokes,
            backgroundConfig = current.backgroundConfig
        )
        redoStack.add(currentSnapshot)

        val previousSnapshot = undoStack.removeAt(undoStack.lastIndex)
        _uiState.update { state ->
            state.copy(
                project = state.project.copy(
                    layers = previousSnapshot.layers,
                    drawStrokes = previousSnapshot.drawStrokes,
                    backgroundConfig = previousSnapshot.backgroundConfig
                ),
                selectedLayerId = if (previousSnapshot.layers.any { it.id == state.selectedLayerId }) state.selectedLayerId else null
            )
        }
        updateUndoRedoAvailability()
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val current = _uiState.value.project
        val currentSnapshot = HistorySnapshot(
            layers = current.layers,
            drawStrokes = current.drawStrokes,
            backgroundConfig = current.backgroundConfig
        )
        undoStack.add(currentSnapshot)

        val nextSnapshot = redoStack.removeAt(redoStack.lastIndex)
        _uiState.update { state ->
            state.copy(
                project = state.project.copy(
                    layers = nextSnapshot.layers,
                    drawStrokes = nextSnapshot.drawStrokes,
                    backgroundConfig = nextSnapshot.backgroundConfig
                )
            )
        }
        updateUndoRedoAvailability()
    }

    fun setActiveTool(tool: ActiveEditorTool) {
        _uiState.update {
            it.copy(
                activeTool = if (it.activeTool == tool) ActiveEditorTool.NONE else tool,
                isDrawModeActive = tool == ActiveEditorTool.DRAW
            )
        }
    }

    fun closeActiveTool() {
        _uiState.update {
            it.copy(
                activeTool = ActiveEditorTool.NONE,
                isDrawModeActive = false
            )
        }
    }

    fun selectLayer(layerId: String?) {
        _uiState.update { it.copy(selectedLayerId = layerId) }
    }

    // --- Layer Operations ---

    fun addTextLayer(
        text: String = "Double Tap to Edit",
        fontFamily: TextFontFamily = TextFontFamily.DEFAULT,
        textColor: Long = 0xFFFFFFFF,
        fontSizeSp: Float = 32f,
        isBold: Boolean = false
    ) {
        pushHistorySnapshot()
        val newLayer = CanvasLayer(
            id = UUID.randomUUID().toString(),
            name = "Text: ${text.take(12)}",
            type = LayerType.TEXT,
            text = text,
            fontFamily = fontFamily,
            textColor = textColor,
            fontSizeSp = fontSizeSp,
            isBold = isBold,
            xPercent = 0.5f,
            yPercent = 0.5f,
            widthPercent = 0.7f,
            heightPercent = 0.15f
        )
        _uiState.update { state ->
            state.copy(
                project = state.project.copy(layers = state.project.layers + newLayer),
                selectedLayerId = newLayer.id,
                activeTool = ActiveEditorTool.TEXT
            )
        }
    }

    fun addImageLayer(uri: String) {
        pushHistorySnapshot()
        val newLayer = CanvasLayer(
            id = UUID.randomUUID().toString(),
            name = "Image Layer",
            type = LayerType.IMAGE,
            imageUri = uri,
            xPercent = 0.5f,
            yPercent = 0.5f,
            widthPercent = 0.6f,
            heightPercent = 0.35f
        )
        _uiState.update { state ->
            state.copy(
                project = state.project.copy(layers = state.project.layers + newLayer),
                selectedLayerId = newLayer.id,
                activeTool = ActiveEditorTool.IMAGE
            )
        }
    }

    fun addShapeLayer(
        shapeType: ShapeType,
        fillColor: Long = 0xFF6366F1,
        borderColor: Long = 0x00000000,
        borderWidthDp: Float = 0f
    ) {
        pushHistorySnapshot()
        val newLayer = CanvasLayer(
            id = UUID.randomUUID().toString(),
            name = "${shapeType.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }}",
            type = LayerType.SHAPE,
            shapeType = shapeType,
            fillColor = fillColor,
            shapeBorderColor = borderColor,
            shapeBorderWidthDp = borderWidthDp,
            xPercent = 0.5f,
            yPercent = 0.5f,
            widthPercent = if (shapeType == ShapeType.LINE) 0.6f else 0.4f,
            heightPercent = if (shapeType == ShapeType.LINE) 0.04f else 0.25f
        )
        _uiState.update { state ->
            state.copy(
                project = state.project.copy(layers = state.project.layers + newLayer),
                selectedLayerId = newLayer.id,
                activeTool = ActiveEditorTool.SHAPES
            )
        }
    }

    fun addStickerLayer(category: String, symbol: String, color: Long = 0xFFFFFFFF) {
        pushHistorySnapshot()
        val newLayer = CanvasLayer(
            id = UUID.randomUUID().toString(),
            name = "$category Sticker $symbol",
            type = LayerType.STICKER,
            stickerCategory = category,
            stickerSymbol = symbol,
            stickerColor = color,
            xPercent = 0.5f,
            yPercent = 0.5f,
            widthPercent = if (symbol.length > 2) 0.45f else 0.25f,
            heightPercent = if (symbol.length > 2) 0.12f else 0.25f
        )
        _uiState.update { state ->
            state.copy(
                project = state.project.copy(layers = state.project.layers + newLayer),
                selectedLayerId = newLayer.id,
                activeTool = ActiveEditorTool.STICKERS
            )
        }
    }

    fun updateSelectedLayer(transform: (CanvasLayer) -> CanvasLayer) {
        val currentId = _uiState.value.selectedLayerId ?: return
        pushHistorySnapshot()
        _uiState.update { state ->
            val updatedLayers = state.project.layers.map { layer ->
                if (layer.id == currentId) transform(layer) else layer
            }
            state.copy(project = state.project.copy(layers = updatedLayers))
        }
    }

    fun updateLayerDirect(updatedLayer: CanvasLayer) {
        _uiState.update { state ->
            val updatedLayers = state.project.layers.map { layer ->
                if (layer.id == updatedLayer.id) updatedLayer else layer
            }
            state.copy(project = state.project.copy(layers = updatedLayers))
        }
    }

    fun commitLayerChange() {
        pushHistorySnapshot()
    }

    fun deleteSelectedLayer() {
        val currentId = _uiState.value.selectedLayerId ?: return
        deleteLayer(currentId)
    }

    fun deleteLayer(layerId: String) {
        pushHistorySnapshot()
        _uiState.update { state ->
            val newLayers = state.project.layers.filterNot { it.id == layerId }
            state.copy(
                project = state.project.copy(layers = newLayers),
                selectedLayerId = if (state.selectedLayerId == layerId) null else state.selectedLayerId
            )
        }
    }

    fun duplicateSelectedLayer() {
        val currentId = _uiState.value.selectedLayerId ?: return
        duplicateLayer(currentId)
    }

    fun duplicateLayer(layerId: String) {
        val layer = _uiState.value.project.layers.find { it.id == layerId } ?: return
        pushHistorySnapshot()
        val duplicated = layer.copy(
            id = UUID.randomUUID().toString(),
            name = "${layer.name} Copy",
            xPercent = (layer.xPercent + 0.04f).coerceAtMost(0.9f),
            yPercent = (layer.yPercent + 0.04f).coerceAtMost(0.9f)
        )
        _uiState.update { state ->
            val index = state.project.layers.indexOfFirst { it.id == layerId }
            val newLayers = state.project.layers.toMutableList()
            newLayers.add(index + 1, duplicated)
            state.copy(
                project = state.project.copy(layers = newLayers),
                selectedLayerId = duplicated.id
            )
        }
    }

    fun toggleLayerVisibility(layerId: String) {
        pushHistorySnapshot()
        _uiState.update { state ->
            val newLayers = state.project.layers.map {
                if (it.id == layerId) it.copy(isVisible = !it.isVisible) else it
            }
            state.copy(project = state.project.copy(layers = newLayers))
        }
    }

    fun toggleLayerLock(layerId: String) {
        _uiState.update { state ->
            val newLayers = state.project.layers.map {
                if (it.id == layerId) it.copy(isLocked = !it.isLocked) else it
            }
            state.copy(project = state.project.copy(layers = newLayers))
        }
    }

    fun moveLayerUp(layerId: String) {
        val layers = _uiState.value.project.layers.toMutableList()
        val index = layers.indexOfFirst { it.id == layerId }
        if (index >= 0 && index < layers.size - 1) {
            pushHistorySnapshot()
            val item = layers.removeAt(index)
            layers.add(index + 1, item)
            _uiState.update { it.copy(project = it.project.copy(layers = layers)) }
        }
    }

    fun moveLayerDown(layerId: String) {
        val layers = _uiState.value.project.layers.toMutableList()
        val index = layers.indexOfFirst { it.id == layerId }
        if (index > 0) {
            pushHistorySnapshot()
            val item = layers.removeAt(index)
            layers.add(index - 1, item)
            _uiState.update { it.copy(project = it.project.copy(layers = layers)) }
        }
    }

    fun bringLayerToFront(layerId: String) {
        val layers = _uiState.value.project.layers.toMutableList()
        val index = layers.indexOfFirst { it.id == layerId }
        if (index >= 0 && index < layers.size - 1) {
            pushHistorySnapshot()
            val item = layers.removeAt(index)
            layers.add(item)
            _uiState.update { it.copy(project = it.project.copy(layers = layers)) }
        }
    }

    fun sendLayerToBack(layerId: String) {
        val layers = _uiState.value.project.layers.toMutableList()
        val index = layers.indexOfFirst { it.id == layerId }
        if (index > 0) {
            pushHistorySnapshot()
            val item = layers.removeAt(index)
            layers.add(0, item)
            _uiState.update { it.copy(project = it.project.copy(layers = layers)) }
        }
    }

    // --- Drawing Tool ---

    fun setDrawTool(tool: DrawToolType) {
        _uiState.update { it.copy(currentDrawTool = tool) }
    }

    fun setDrawColor(color: Long) {
        _uiState.update { it.copy(currentDrawColor = color) }
    }

    fun setBrushSize(sizePercent: Float) {
        _uiState.update { it.copy(currentBrushSizePercent = sizePercent) }
    }

    fun setBrushOpacity(opacity: Float) {
        _uiState.update { it.copy(currentBrushOpacity = opacity) }
    }

    fun onDrawingStart(point: PointF2) {
        _uiState.update { it.copy(currentDrawingPoints = listOf(point)) }
    }

    fun onDrawingMove(point: PointF2) {
        _uiState.update { it.copy(currentDrawingPoints = it.currentDrawingPoints + point) }
    }

    fun onDrawingEnd() {
        val points = _uiState.value.currentDrawingPoints
        if (points.size >= 2) {
            pushHistorySnapshot()
            val stroke = DrawStroke(
                id = UUID.randomUUID().toString(),
                points = points,
                toolType = _uiState.value.currentDrawTool,
                color = _uiState.value.currentDrawColor,
                strokeWidthPercent = _uiState.value.currentBrushSizePercent,
                opacity = _uiState.value.currentBrushOpacity
            )
            _uiState.update { state ->
                state.copy(
                    project = state.project.copy(drawStrokes = state.project.drawStrokes + stroke),
                    currentDrawingPoints = emptyList()
                )
            }
        } else {
            _uiState.update { it.copy(currentDrawingPoints = emptyList()) }
        }
    }

    fun clearAllStrokes() {
        if (_uiState.value.project.drawStrokes.isNotEmpty()) {
            pushHistorySnapshot()
            _uiState.update { state ->
                state.copy(project = state.project.copy(drawStrokes = emptyList()))
            }
        }
    }

    // --- Background Operations ---

    fun updateBackground(config: BackgroundConfig) {
        pushHistorySnapshot()
        _uiState.update { state ->
            state.copy(project = state.project.copy(backgroundConfig = config))
        }
    }

    // --- Canvas View Controls ---

    fun updateZoom(zoom: Float) {
        _uiState.update { it.copy(canvasZoom = zoom.coerceIn(0.5f, 4.0f)) }
    }

    fun updatePan(dx: Float, dy: Float) {
        _uiState.update { it.copy(canvasPanX = it.canvasPanX + dx, canvasPanY = it.canvasPanY + dy) }
    }

    fun fitToScreen() {
        _uiState.update {
            it.copy(
                canvasZoom = 1.0f,
                canvasPanX = 0f,
                canvasPanY = 0f
            )
        }
    }

    // --- Dialogs & Project Management ---

    fun setShowExportDialog(show: Boolean) {
        _uiState.update { it.copy(showExportDialog = show) }
    }

    fun setShowRenameDialog(show: Boolean) {
        _uiState.update { it.copy(showRenameDialog = show) }
    }

    fun setShowQualityEnhanceDialog(show: Boolean) {
        _uiState.update { it.copy(showQualityEnhanceDialog = show) }
    }

    fun applyEnhancedLayer(enhancedUri: String) {
        val originalLayer = selectedLayer
        pushHistorySnapshot()
        val newLayer = if (originalLayer != null && originalLayer.type == LayerType.IMAGE) {
            originalLayer.copy(
                id = UUID.randomUUID().toString(),
                name = "Enhanced ${originalLayer.name}",
                imageUri = enhancedUri,
                xPercent = originalLayer.xPercent,
                yPercent = originalLayer.yPercent,
                widthPercent = originalLayer.widthPercent,
                heightPercent = originalLayer.heightPercent,
                adjustments = ImageAdjustments() // Fresh clean adjustments for enhanced bitmap
            )
        } else {
            CanvasLayer(
                id = UUID.randomUUID().toString(),
                name = "Enhanced Photo",
                type = LayerType.IMAGE,
                imageUri = enhancedUri,
                xPercent = 0.5f,
                yPercent = 0.5f,
                widthPercent = 0.6f,
                heightPercent = 0.35f
            )
        }

        _uiState.update { state ->
            val index = if (originalLayer != null) state.project.layers.indexOfFirst { it.id == originalLayer.id } else state.project.layers.lastIndex
            val newLayers = state.project.layers.toMutableList()
            if (index >= 0 && index < newLayers.size) {
                newLayers.add(index + 1, newLayer)
            } else {
                newLayers.add(newLayer)
            }
            state.copy(
                project = state.project.copy(layers = newLayers),
                selectedLayerId = newLayer.id,
                showQualityEnhanceDialog = false,
                toastMessage = "Enhanced photo added as new layer ✨"
            )
        }
    }

    fun renameProject(newTitle: String) {
        if (newTitle.isNotBlank()) {
            _uiState.update { state ->
                state.copy(
                    project = state.project.copy(title = newTitle),
                    showRenameDialog = false
                )
            }
            saveProjectSilently()
        }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun saveProject(context: Context? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                // Generate a lightweight base64 thumbnail if context is available
                var thumbBase64: String? = null
                if (context != null) {
                    try {
                        val previewBitmap = CanvasExporter.renderProjectToBitmap(
                            context,
                            _uiState.value.project,
                            scaleMultiplier = 0.2f
                        )
                        val stream = ByteArrayOutputStream()
                        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
                        thumbBase64 = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
                    } catch (e: Exception) {
                        // ignore thumbnail generation failure
                    }
                }

                val projectToSave = _uiState.value.project.copy(
                    thumbnailBase64 = thumbBase64 ?: _uiState.value.project.thumbnailBase64
                )
                val savedId = repository.saveProject(projectToSave)
                _uiState.update {
                    it.copy(
                        project = projectToSave.copy(id = savedId),
                        isSaving = false,
                        toastMessage = "Project saved successfully!"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        toastMessage = "Failed to save: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    private fun saveProjectSilently() {
        viewModelScope.launch {
            try {
                repository.saveProject(_uiState.value.project)
            } catch (e: Exception) {
                // silent
            }
        }
    }

    fun exportProject(
        context: Context,
        options: ExportOptions,
        isShare: Boolean
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, showExportDialog = false) }
            try {
                val bitmap = CanvasExporter.renderProjectToBitmap(
                    context,
                    _uiState.value.project,
                    options.scaleMultiplier
                )

                if (isShare) {
                    CanvasExporter.exportAndShare(
                        context,
                        bitmap,
                        _uiState.value.project.title,
                        options.format
                    )
                    _uiState.update {
                        it.copy(isExporting = false, toastMessage = "Shared successfully!")
                    }
                } else {
                    val uri = CanvasExporter.saveBitmapToGallery(
                        context,
                        bitmap,
                        _uiState.value.project.title,
                        options.format,
                        options.qualityPercent
                    )
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            toastMessage = if (uri != null) "Saved to Gallery (Pictures/PixCraftPro)" else "Exported successfully!"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        toastMessage = "Export failed: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    companion object {
        fun provideFactory(
            repository: ProjectRepository,
            project: Project
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return EditorViewModel(repository, project) as T
            }
        }
    }
}
