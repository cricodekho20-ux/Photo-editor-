package com.example.ui.editor

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.LayerType
import com.example.ui.theme.StudioPrimary
import kotlin.math.roundToInt

@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    BackHandler {
        viewModel.saveProject(context)
        onNavigateBack()
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF08090E))
    ) {
        val isWideScreen = maxWidth > 600.dp

        if (isWideScreen) {
            // Tablet / Desktop Layout
            Row(modifier = Modifier.fillMaxSize()) {
                // Left Tools Rail
                Surface(
                    color = Color(0xFF10131D),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF262C3E)),
                    modifier = Modifier
                        .width(96.dp)
                        .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IconButton(onClick = {
                            viewModel.saveProject(context)
                            onNavigateBack()
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }

                        Divider(color = Color(0xFF262C3E), modifier = Modifier.padding(horizontal = 12.dp))

                        for (item in TOOLBAR_ITEMS) {
                            val isSelected = uiState.activeTool == item.tool
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) StudioPrimary.copy(alpha = 0.2f) else Color.Transparent)
                                    .clickable { viewModel.setActiveTool(item.tool) }
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    tint = if (isSelected) StudioPrimary else Color(0xFF94A3B8)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(item.label, color = if (isSelected) StudioPrimary else Color.White, fontSize = 10.sp)
                            }
                        }
                    }
                }

                // Center Canvas Area with Top Bar
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    EditorTopBar(
                        projectTitle = uiState.project.title,
                        canUndo = uiState.canUndo,
                        canRedo = uiState.canRedo,
                        isSaving = uiState.isSaving,
                        onBackClick = {
                            viewModel.saveProject(context)
                            onNavigateBack()
                        },
                        onTitleClick = { viewModel.setShowRenameDialog(true) },
                        onUndoClick = { viewModel.undo() },
                        onRedoClick = { viewModel.redo() },
                        onSaveClick = { viewModel.saveProject(context) },
                        onExportClick = { viewModel.setShowExportDialog(true) }
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        EditorCanvasViewport(
                            uiState = uiState,
                            viewModel = viewModel
                        )
                    }
                }

                // Right Properties Pane if active tool selected
                if (uiState.activeTool != ActiveEditorTool.NONE) {
                    Surface(
                        color = Color(0xFF12151F),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF262C3E)),
                        modifier = Modifier
                            .width(330.dp)
                            .fillMaxHeight()
                    ) {
                        EditorToolSheet(
                            activeTool = uiState.activeTool,
                            selectedLayer = viewModel.selectedLayer,
                            allLayers = uiState.project.layers,
                            backgroundConfig = uiState.project.backgroundConfig,
                            currentDrawTool = uiState.currentDrawTool,
                            currentDrawColor = uiState.currentDrawColor,
                            currentBrushSizePercent = uiState.currentBrushSizePercent,
                            currentBrushOpacity = uiState.currentBrushOpacity,
                            onClose = { viewModel.closeActiveTool() },
                            onAddText = { text, font, color, size, bold ->
                                viewModel.addTextLayer(text, font, color, size, bold)
                            },
                            onAddImage = { uri -> viewModel.addImageLayer(uri) },
                            onOpenQualityEnhance = { viewModel.setShowQualityEnhanceDialog(true) },
                            onAddShape = { type, fill, border, bw ->
                                viewModel.addShapeLayer(type, fill, border, bw)
                            },
                            onAddSticker = { cat, sym, color ->
                                viewModel.addStickerLayer(cat, sym, color)
                            },
                            onUpdateSelectedLayer = { transform ->
                                viewModel.updateSelectedLayer(transform)
                            },
                            onSelectLayer = { id -> viewModel.selectLayer(id) },
                            onDeleteLayer = { id -> viewModel.deleteLayer(id) },
                            onDuplicateLayer = { id -> viewModel.duplicateLayer(id) },
                            onToggleVisibility = { id -> viewModel.toggleLayerVisibility(id) },
                            onToggleLock = { id -> viewModel.toggleLayerLock(id) },
                            onMoveLayerUp = { id -> viewModel.moveLayerUp(id) },
                            onMoveLayerDown = { id -> viewModel.moveLayerDown(id) },
                            onSetDrawTool = { tool -> viewModel.setDrawTool(tool) },
                            onSetDrawColor = { color -> viewModel.setDrawColor(color) },
                            onSetBrushSize = { size -> viewModel.setBrushSize(size) },
                            onSetBrushOpacity = { op -> viewModel.setBrushOpacity(op) },
                            onClearStrokes = { viewModel.clearAllStrokes() },
                            onUpdateBackground = { config -> viewModel.updateBackground(config) }
                        )
                    }
                }
            }
        } else {
            // Mobile Phone Layout (360-430px)
            Scaffold(
                topBar = {
                    EditorTopBar(
                        projectTitle = uiState.project.title,
                        canUndo = uiState.canUndo,
                        canRedo = uiState.canRedo,
                        isSaving = uiState.isSaving,
                        onBackClick = {
                            viewModel.saveProject(context)
                            onNavigateBack()
                        },
                        onTitleClick = { viewModel.setShowRenameDialog(true) },
                        onUndoClick = { viewModel.undo() },
                        onRedoClick = { viewModel.redo() },
                        onSaveClick = { viewModel.saveProject(context) },
                        onExportClick = { viewModel.setShowExportDialog(true) }
                    )
                },
                bottomBar = {
                    Column {
                        AnimatedVisibility(
                            visible = uiState.activeTool != ActiveEditorTool.NONE,
                            enter = slideInVertically { it } + fadeIn(),
                            exit = slideOutVertically { it } + fadeOut()
                        ) {
                            EditorToolSheet(
                                activeTool = uiState.activeTool,
                                selectedLayer = viewModel.selectedLayer,
                                allLayers = uiState.project.layers,
                                backgroundConfig = uiState.project.backgroundConfig,
                                currentDrawTool = uiState.currentDrawTool,
                                currentDrawColor = uiState.currentDrawColor,
                                currentBrushSizePercent = uiState.currentBrushSizePercent,
                                currentBrushOpacity = uiState.currentBrushOpacity,
                                onClose = { viewModel.closeActiveTool() },
                                onAddText = { text, font, color, size, bold ->
                                    viewModel.addTextLayer(text, font, color, size, bold)
                                },
                                onAddImage = { uri -> viewModel.addImageLayer(uri) },
                                onOpenQualityEnhance = { viewModel.setShowQualityEnhanceDialog(true) },
                                onAddShape = { type, fill, border, bw ->
                                    viewModel.addShapeLayer(type, fill, border, bw)
                                },
                                onAddSticker = { cat, sym, color ->
                                    viewModel.addStickerLayer(cat, sym, color)
                                },
                                onUpdateSelectedLayer = { transform ->
                                    viewModel.updateSelectedLayer(transform)
                                },
                                onSelectLayer = { id -> viewModel.selectLayer(id) },
                                onDeleteLayer = { id -> viewModel.deleteLayer(id) },
                                onDuplicateLayer = { id -> viewModel.duplicateLayer(id) },
                                onToggleVisibility = { id -> viewModel.toggleLayerVisibility(id) },
                                onToggleLock = { id -> viewModel.toggleLayerLock(id) },
                                onMoveLayerUp = { id -> viewModel.moveLayerUp(id) },
                                onMoveLayerDown = { id -> viewModel.moveLayerDown(id) },
                                onSetDrawTool = { tool -> viewModel.setDrawTool(tool) },
                                onSetDrawColor = { color -> viewModel.setDrawColor(color) },
                                onSetBrushSize = { size -> viewModel.setBrushSize(size) },
                                onSetBrushOpacity = { op -> viewModel.setBrushOpacity(op) },
                                onClearStrokes = { viewModel.clearAllStrokes() },
                                onUpdateBackground = { config -> viewModel.updateBackground(config) }
                            )
                        }

                        EditorBottomToolbar(
                            activeTool = uiState.activeTool,
                            onToolClick = { tool -> viewModel.setActiveTool(tool) }
                        )
                    }
                },
                snackbarHost = { SnackbarHost(snackbarHostState) },
                containerColor = Color(0xFF08090E)
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    EditorCanvasViewport(
                        uiState = uiState,
                        viewModel = viewModel
                    )
                }
            }
        }

        // Photo Quality Enhancer Dialog (AI Sharpening, Detail Recovery, Super-Resolution 2x/4x)
        if (uiState.showQualityEnhanceDialog) {
            val imageLayer = viewModel.selectedLayer?.takeIf { it.type == LayerType.IMAGE }
                ?: uiState.project.layers.firstOrNull { it.type == LayerType.IMAGE }

            if (imageLayer != null) {
                PhotoQualityEnhanceDialog(
                    layer = imageLayer,
                    onDismiss = { viewModel.setShowQualityEnhanceDialog(false) },
                    onApplyEnhancedLayer = { enhancedUri, _, _ ->
                        viewModel.applyEnhancedLayer(enhancedUri)
                    }
                )
            }
        }

        // Export Dialog
        if (uiState.showExportDialog) {
            ExportDialog(
                project = uiState.project,
                onDismiss = { viewModel.setShowExportDialog(false) },
                onExportSave = { options -> viewModel.exportProject(context, options, isShare = false) },
                onExportShare = { options -> viewModel.exportProject(context, options, isShare = true) }
            )
        }

        // Rename Dialog
        if (uiState.showRenameDialog) {
            RenameDialog(
                currentTitle = uiState.project.title,
                onDismiss = { viewModel.setShowRenameDialog(false) },
                onConfirm = { newTitle -> viewModel.renameProject(newTitle) }
            )
        }

        // Exporting progress overlay
        if (uiState.isExporting) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF151924),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF262C3E)),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = StudioPrimary)
                        Text(
                            "Rendering High-Res Artwork...",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EditorCanvasViewport(
    uiState: EditorUiState,
    viewModel: EditorViewModel
) {
    Box(modifier = Modifier.fillMaxSize()) {
        EditorCanvas(
            project = uiState.project,
            selectedLayerId = uiState.selectedLayerId,
            isDrawModeActive = uiState.isDrawModeActive,
            currentDrawingPoints = uiState.currentDrawingPoints,
            currentDrawTool = uiState.currentDrawTool,
            currentDrawColor = uiState.currentDrawColor,
            currentBrushSizePercent = uiState.currentBrushSizePercent,
            currentBrushOpacity = uiState.currentBrushOpacity,
            canvasZoom = uiState.canvasZoom,
            canvasPanX = uiState.canvasPanX,
            canvasPanY = uiState.canvasPanY,
            onSelectLayer = { id -> viewModel.selectLayer(id) },
            onUpdateLayerDirect = { layer -> viewModel.updateLayerDirect(layer) },
            onCommitLayerChange = { viewModel.commitLayerChange() },
            onDeleteSelectedLayer = { viewModel.deleteSelectedLayer() },
            onDuplicateSelectedLayer = { viewModel.duplicateSelectedLayer() },
            onMoveLayerUp = { id -> viewModel.moveLayerUp(id) },
            onMoveLayerDown = { id -> viewModel.moveLayerDown(id) },
            onToggleLayerLock = { id -> viewModel.toggleLayerLock(id) },
            onDrawingStart = { pt -> viewModel.onDrawingStart(pt) },
            onDrawingMove = { pt -> viewModel.onDrawingMove(pt) },
            onDrawingEnd = { viewModel.onDrawingEnd() },
            onZoomChange = { zoom -> viewModel.updateZoom(zoom) },
            onPanChange = { dx, dy -> viewModel.updatePan(dx, dy) }
        )

        // Floating HUD overlay (Fit to Screen & Zoom Indicator)
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (uiState.isDrawModeActive) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF43F5E),
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Brush, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Draw Mode", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF151924).copy(alpha = 0.9f),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF262C3E)),
                modifier = Modifier.clickable { viewModel.fitToScreen() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CropFree,
                        contentDescription = "Fit to Screen",
                        tint = StudioPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${(uiState.canvasZoom * 100).roundToInt()}%",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
