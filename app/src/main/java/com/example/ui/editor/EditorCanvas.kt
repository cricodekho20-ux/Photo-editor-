package com.example.ui.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.*
import com.example.ui.theme.StudioPrimary
import com.example.util.FontHelper
import com.example.util.PhotoEnhancer
import com.example.util.ShapeDefinitions
import kotlin.math.*

@Composable
fun EditorCanvas(
    project: Project,
    selectedLayerId: String?,
    isDrawModeActive: Boolean,
    currentDrawingPoints: List<PointF2>,
    currentDrawTool: DrawToolType,
    currentDrawColor: Long,
    currentBrushSizePercent: Float,
    currentBrushOpacity: Float,
    canvasZoom: Float,
    canvasPanX: Float,
    canvasPanY: Float,
    onSelectLayer: (String?) -> Unit,
    onUpdateLayerDirect: (CanvasLayer) -> Unit,
    onCommitLayerChange: () -> Unit,
    onDeleteSelectedLayer: () -> Unit,
    onDuplicateSelectedLayer: () -> Unit,
    onMoveLayerUp: (String) -> Unit,
    onMoveLayerDown: (String) -> Unit,
    onToggleLayerLock: (String) -> Unit,
    onOpenQualityEnhance: () -> Unit = {},
    onDrawingStart: (PointF2) -> Unit,
    onDrawingMove: (PointF2) -> Unit,
    onDrawingEnd: () -> Unit,
    onZoomChange: (Float) -> Unit,
    onPanChange: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF08090E))
            .clipToBounds()
            .pointerInput(isDrawModeActive) {
                if (!isDrawModeActive) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        onZoomChange(canvasZoom * zoom)
                        onPanChange(pan.x, pan.y)
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        if (!isDrawModeActive) {
                            onSelectLayer(null)
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        val containerWidthPx = constraints.maxWidth.toFloat()
        val containerHeightPx = constraints.maxHeight.toFloat()

        // 100% precise screen fit calculation
        val marginHorizontal = 24f
        val marginVertical = 24f
        val availableW = (containerWidthPx - marginHorizontal * 2).coerceAtLeast(100f)
        val availableH = (containerHeightPx - marginVertical * 2).coerceAtLeast(100f)

        val projectAspect = project.width.toFloat() / project.height.toFloat()
        val (canvasWidthPx, canvasHeightPx) = if (availableW / availableH > projectAspect) {
            val h = availableH
            val w = h * projectAspect
            Pair(w, h)
        } else {
            val w = availableW
            val h = w / projectAspect
            Pair(w, h)
        }

        val canvasWidthDp = with(density) { canvasWidthPx.toDp() }
        val canvasHeightDp = with(density) { canvasHeightPx.toDp() }

        // Scaled and Panned Canvas Container
        Box(
            modifier = Modifier
                .offset { IntOffset(canvasPanX.roundToInt(), canvasPanY.roundToInt()) }
                .size(canvasWidthDp * canvasZoom, canvasHeightDp * canvasZoom)
                .shadow(elevation = 20.dp, shape = RoundedCornerShape(4.dp))
        ) {
            // Actual Canvas Surface
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(2.dp))
            ) {
                // 1. Background Renderer
                CanvasBackgroundRenderer(
                    config = project.backgroundConfig,
                    modifier = Modifier.fillMaxSize()
                )

                // 2. Layers Rendering (Bottom to Top)
                for (layer in project.layers) {
                    if (!layer.isVisible) continue
                    val isSelected = (layer.id == selectedLayerId)

                    CanvasLayerItem(
                        layer = layer,
                        isSelected = isSelected,
                        canvasWidthPx = canvasWidthPx * canvasZoom,
                        canvasHeightPx = canvasHeightPx * canvasZoom,
                        isDrawMode = isDrawModeActive,
                        onSelect = { onSelectLayer(layer.id) },
                        onUpdateLayerDirect = onUpdateLayerDirect,
                        onCommitLayerChange = onCommitLayerChange,
                        onDelete = onDeleteSelectedLayer,
                        onDuplicate = onDuplicateSelectedLayer,
                        onMoveUp = { onMoveLayerUp(layer.id) },
                        onMoveDown = { onMoveLayerDown(layer.id) },
                        onToggleLock = { onToggleLayerLock(layer.id) },
                        onOpenQualityEnhance = onOpenQualityEnhance
                    )
                }

                // 3. Freehand Drawing Strokes & Current Live Stroke
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(isDrawModeActive) {
                            if (isDrawModeActive) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        val normX = (offset.x / size.width).coerceIn(0f, 1f)
                                        val normY = (offset.y / size.height).coerceIn(0f, 1f)
                                        onDrawingStart(PointF2(normX, normY))
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        val normX = (change.position.x / size.width).coerceIn(0f, 1f)
                                        val normY = (change.position.y / size.height).coerceIn(0f, 1f)
                                        onDrawingMove(PointF2(normX, normY))
                                    },
                                    onDragEnd = { onDrawingEnd() },
                                    onDragCancel = { onDrawingEnd() }
                                )
                            }
                        }
                ) {
                    val cw = size.width
                    val ch = size.height

                    // Draw completed strokes
                    for (stroke in project.drawStrokes) {
                        drawStrokeItem(stroke, cw, ch)
                    }

                    // Draw currently active stroke
                    if (currentDrawingPoints.size >= 2) {
                        val activeStroke = DrawStroke(
                            id = "active_stroke",
                            points = currentDrawingPoints,
                            toolType = currentDrawTool,
                            color = currentDrawColor,
                            strokeWidthPercent = currentBrushSizePercent,
                            opacity = currentBrushOpacity
                        )
                        drawStrokeItem(activeStroke, cw, ch)
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawStrokeItem(stroke: DrawStroke, cw: Float, ch: Float) {
    if (stroke.points.size < 2) return
    val path = Path()
    val first = stroke.points.first()
    path.moveTo(first.x * cw, first.y * ch)

    for (i in 1 until stroke.points.size) {
        val pt = stroke.points[i]
        val prev = stroke.points[i - 1]
        val cx = (prev.x + pt.x) / 2f * cw
        val cy = (prev.y + pt.y) / 2f * ch
        path.quadraticTo(prev.x * cw, prev.y * ch, cx, cy)
    }

    val last = stroke.points.last()
    path.lineTo(last.x * cw, last.y * ch)

    val strokeWidthPx = (stroke.strokeWidthPercent * cw).coerceAtLeast(2f)
    val strokeColor = Color(stroke.color).copy(alpha = stroke.opacity.coerceIn(0f, 1f))

    val blendMode = when (stroke.toolType) {
        DrawToolType.HIGHLIGHTER -> BlendMode.SrcOver
        DrawToolType.ERASER -> BlendMode.Clear
        else -> BlendMode.SrcOver
    }

    drawPath(
        path = path,
        color = strokeColor,
        style = Stroke(
            width = strokeWidthPx,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        ),
        blendMode = blendMode
    )
}

@Composable
private fun CanvasBackgroundRenderer(
    config: BackgroundConfig,
    modifier: Modifier = Modifier
) {
    when (config.type) {
        BackgroundType.SOLID -> {
            Box(modifier = modifier.background(Color(config.solidColor)))
        }
        BackgroundType.GRADIENT -> {
            val colors = if (config.gradientColors.isNotEmpty()) {
                config.gradientColors.map { Color(it) }
            } else {
                listOf(Color(0xFF7C3AED), Color(0xFFFFB300))
            }
            Canvas(modifier = modifier) {
                val angleRad = Math.toRadians(config.gradientAngle.toDouble())
                val w = size.width
                val h = size.height
                val start = Offset(
                    (w / 2f - (w / 2f) * cos(angleRad)).toFloat(),
                    (h / 2f - (h / 2f) * sin(angleRad)).toFloat()
                )
                val end = Offset(
                    (w / 2f + (w / 2f) * cos(angleRad)).toFloat(),
                    (h / 2f + (h / 2f) * sin(angleRad)).toFloat()
                )
                drawRect(
                    brush = Brush.linearGradient(colors = colors, start = start, end = end),
                    size = size
                )
            }
        }
        BackgroundType.IMAGE -> {
            if (!config.imageUri.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(config.imageUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Canvas Background",
                    contentScale = ContentScale.Crop,
                    modifier = modifier.fillMaxSize()
                )
            } else {
                Box(modifier = modifier.background(Color.Black))
            }
        }
        BackgroundType.TRANSPARENT -> {
            Canvas(modifier = modifier) {
                val checkSize = 16.dp.toPx()
                val numCols = ceil(size.width / checkSize).toInt()
                val numRows = ceil(size.height / checkSize).toInt()
                for (r in 0 until numRows) {
                    for (c in 0 until numCols) {
                        val isLight = (r + c) % 2 == 0
                        drawRect(
                            color = if (isLight) Color(0xFF1E2230) else Color(0xFF131620),
                            topLeft = Offset(c * checkSize, r * checkSize),
                            size = Size(checkSize, checkSize)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CanvasLayerItem(
    layer: CanvasLayer,
    isSelected: Boolean,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
    isDrawMode: Boolean,
    onSelect: () -> Unit,
    onUpdateLayerDirect: (CanvasLayer) -> Unit,
    onCommitLayerChange: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onToggleLock: () -> Unit,
    onOpenQualityEnhance: () -> Unit
) {
    val density = LocalDensity.current

    val layerWidthPx = layer.widthPercent * canvasWidthPx
    val layerHeightPx = layer.heightPercent * canvasHeightPx
    val centerX = layer.xPercent * canvasWidthPx
    val centerY = layer.yPercent * canvasHeightPx

    val leftPx = centerX - layerWidthPx / 2f
    val topPx = centerY - layerHeightPx / 2f

    val leftDp = with(density) { leftPx.toDp() }
    val topDp = with(density) { topPx.toDp() }
    val widthDp = with(density) { layerWidthPx.toDp() }
    val heightDp = with(density) { layerHeightPx.toDp() }

    Box(
        modifier = Modifier
            .offset(x = leftDp, y = topDp)
            .size(widthDp, heightDp)
            .graphicsLayer {
                rotationZ = layer.rotationDeg
                alpha = layer.opacity.coerceIn(0f, 1f)
            }
            .pointerInput(isDrawMode, layer.isLocked) {
                if (!isDrawMode && !layer.isLocked) {
                    detectTapGestures(onTap = { onSelect() })
                }
            }
            .pointerInput(isDrawMode, layer.isLocked) {
                if (!isDrawMode && !layer.isLocked) {
                    detectDragGestures(
                        onDragStart = { onSelect() },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val deltaXPercent = dragAmount.x / canvasWidthPx
                            val deltaYPercent = dragAmount.y / canvasHeightPx
                            onUpdateLayerDirect(
                                layer.copy(
                                    xPercent = (layer.xPercent + deltaXPercent).coerceIn(0.05f, 0.95f),
                                    yPercent = (layer.yPercent + deltaYPercent).coerceIn(0.05f, 0.95f)
                                )
                            )
                        },
                        onDragEnd = { onCommitLayerChange() }
                    )
                }
            }
    ) {
        // --- Layer Content Rendering ---
        when (layer.type) {
            LayerType.TEXT -> {
                TextLayerContent(layer = layer, modifier = Modifier.fillMaxSize())
            }
            LayerType.IMAGE -> {
                ImageLayerContent(layer = layer, modifier = Modifier.fillMaxSize())
            }
            LayerType.SHAPE -> {
                ShapeLayerContent(layer = layer, modifier = Modifier.fillMaxSize())
            }
            LayerType.STICKER -> {
                StickerLayerContent(layer = layer, modifier = Modifier.fillMaxSize())
            }
        }

        // --- Selection Handles & Quick Controls ---
        if (isSelected && !isDrawMode) {
            SelectionOverlay(
                layer = layer,
                canvasWidthPx = canvasWidthPx,
                canvasHeightPx = canvasHeightPx,
                onUpdateLayerDirect = onUpdateLayerDirect,
                onCommitLayerChange = onCommitLayerChange,
                onDelete = onDelete,
                onDuplicate = onDuplicate,
                onMoveUp = onMoveUp,
                onMoveDown = onMoveDown,
                onToggleLock = onToggleLock,
                onOpenQualityEnhance = onOpenQualityEnhance
            )
        }
    }
}

@Composable
private fun TextLayerContent(layer: CanvasLayer, modifier: Modifier = Modifier) {
    val fontFamily = FontHelper.getComposeFontFamily(layer.fontFamily)
    val fontWeight = if (layer.isBold || layer.fontFamily == TextFontFamily.BOLD_DISPLAY || layer.fontFamily == TextFontFamily.HINDI_ROZHA || layer.fontFamily == TextFontFamily.ENGLISH_BEBAS) FontWeight.Bold else FontWeight.Normal
    val fontStyle = if (layer.isItalic) FontStyle.Italic else FontStyle.Normal
    val textAlign = when (layer.textAlign) {
        CanvasTextAlign.LEFT -> TextAlign.Left
        CanvasTextAlign.CENTER -> TextAlign.Center
        CanvasTextAlign.RIGHT -> TextAlign.Right
    }

    val shadow = if (layer.shadowColor != 0x00000000L && layer.shadowRadiusDp > 0f) {
        androidx.compose.ui.graphics.Shadow(
            color = Color(layer.shadowColor),
            offset = Offset(layer.shadowDxDp, layer.shadowDyDp),
            blurRadius = layer.shadowRadiusDp * 2
        )
    } else null

    Box(
        modifier = modifier
            .then(
                if (layer.backgroundColor != 0x00000000L) {
                    Modifier
                        .background(
                            Color(layer.backgroundColor),
                            RoundedCornerShape(layer.backgroundCornerRadiusDp.dp)
                        )
                        .padding(layer.backgroundPaddingDp.dp)
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = layer.text,
            color = Color(layer.textColor),
            fontSize = layer.fontSizeSp.sp,
            fontFamily = fontFamily,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
            textAlign = textAlign,
            style = TextStyle(shadow = shadow),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ImageLayerContent(layer: CanvasLayer, modifier: Modifier = Modifier) {
    if (!layer.imageUri.isNullOrBlank()) {
        val composeColorFilter = PhotoEnhancer.buildComposeColorFilter(layer.adjustments)
        val cornerRadius = layer.imageCornerRadiusDp.dp
        val shape = RoundedCornerShape(cornerRadius)

        Box(
            modifier = modifier
                .fillMaxSize()
                .then(
                    if (layer.imageShadowColor != 0x00000000L && layer.imageShadowRadiusDp > 0f) {
                        Modifier.shadow(
                            elevation = layer.imageShadowRadiusDp.dp,
                            shape = shape,
                            spotColor = Color(layer.imageShadowColor),
                            ambientColor = Color(layer.imageShadowColor)
                        )
                    } else Modifier
                )
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(layer.imageUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Layer Image",
                colorFilter = composeColorFilter,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (layer.imageCornerRadiusDp > 0f) {
                            Modifier.clip(shape)
                        } else Modifier
                    )
                    .then(
                        if (layer.imageBorderColor != 0x00000000L && layer.imageBorderWidthDp > 0f) {
                            Modifier.border(
                                width = layer.imageBorderWidthDp.dp,
                                color = Color(layer.imageBorderColor),
                                shape = shape
                            )
                        } else Modifier
                    )
                    .graphicsLayer {
                        scaleX = if (layer.flipHorizontal) -1f else 1f
                        scaleY = if (layer.flipVertical) -1f else 1f
                    }
            )
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF1E2330), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

@Composable
private fun ShapeLayerContent(layer: CanvasLayer, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val path = ShapeDefinitions.buildComposePath(
            layer.shapeType,
            size,
            cornerRadiusPx = layer.shapeCornerRadiusDp * density
        )

        // Draw Shape Shadow
        if (layer.shapeShadowColor != 0x00000000L && layer.shapeShadowRadiusDp > 0f) {
            drawPath(
                path = path,
                color = Color(layer.shapeShadowColor).copy(alpha = 0.5f)
            )
        }

        // Fill
        if (layer.fillColor != 0x00000000L && layer.shapeType != ShapeType.LINE) {
            drawPath(path = path, color = Color(layer.fillColor))
        }

        // Border / Stroke / Highlight
        if (layer.shapeBorderColor != 0x00000000L && (layer.shapeBorderWidthDp > 0f || layer.shapeType == ShapeType.LINE)) {
            val strokeW = if (layer.shapeType == ShapeType.LINE) {
                layer.shapeBorderWidthDp.coerceAtLeast(3f) * density
            } else {
                layer.shapeBorderWidthDp * density
            }
            drawPath(
                path = path,
                color = Color(layer.shapeBorderColor),
                style = Stroke(
                    width = strokeW,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}

@Composable
private fun StickerLayerContent(layer: CanvasLayer, modifier: Modifier = Modifier) {
    val shadow = if (layer.stickerShadowColor != 0x00000000L && layer.stickerShadowRadiusDp > 0f) {
        androidx.compose.ui.graphics.Shadow(
            color = Color(layer.stickerShadowColor),
            offset = Offset(layer.stickerShadowDxDp, layer.stickerShadowDyDp),
            blurRadius = layer.stickerShadowRadiusDp * 2
        )
    } else null

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = layer.stickerSymbol,
            color = Color(layer.stickerColor),
            fontSize = 36.sp,
            textAlign = TextAlign.Center,
            style = TextStyle(shadow = shadow)
        )
    }
}

@Composable
private fun BoxScope.SelectionOverlay(
    layer: CanvasLayer,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
    onUpdateLayerDirect: (CanvasLayer) -> Unit,
    onCommitLayerChange: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onToggleLock: () -> Unit,
    onOpenQualityEnhance: () -> Unit
) {
    // 1. Selection border outline
    Box(
        modifier = Modifier
            .fillMaxSize()
            .border(1.5.dp, StudioPrimary, RoundedCornerShape(2.dp))
    )

    // 2. Corner Resize Handles
    Box(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .offset(x = 10.dp, y = 10.dp)
            .size(22.dp)
            .background(StudioPrimary, CircleShape)
            .border(2.dp, Color.White, CircleShape)
            .pointerInput(layer.id) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val deltaW = (dragAmount.x / canvasWidthPx)
                        val deltaH = (dragAmount.y / canvasHeightPx)
                        onUpdateLayerDirect(
                            layer.copy(
                                widthPercent = (layer.widthPercent + deltaW).coerceIn(0.1f, 0.95f),
                                heightPercent = (layer.heightPercent + deltaH).coerceIn(0.04f, 0.95f)
                            )
                        )
                    },
                    onDragEnd = { onCommitLayerChange() }
                )
            }
    )

    Box(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .offset(x = 10.dp, y = (-10).dp)
            .size(18.dp)
            .background(Color.White, CircleShape)
            .border(1.5.dp, StudioPrimary, CircleShape)
            .pointerInput(layer.id) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val deltaW = (dragAmount.x / canvasWidthPx)
                        val deltaH = (-dragAmount.y / canvasHeightPx)
                        onUpdateLayerDirect(
                            layer.copy(
                                widthPercent = (layer.widthPercent + deltaW).coerceIn(0.1f, 0.95f),
                                heightPercent = (layer.heightPercent + deltaH).coerceIn(0.04f, 0.95f)
                            )
                        )
                    },
                    onDragEnd = { onCommitLayerChange() }
                )
            }
    )

    // 3. Top Rotation Handle
    Column(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .offset(y = (-36).dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(Color(0xFF7C3AED), CircleShape)
                .border(2.dp, Color.White, CircleShape)
                .pointerInput(layer.id) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val currentRot = layer.rotationDeg
                            val rotChange = (dragAmount.x * 0.75f)
                            onUpdateLayerDirect(
                                layer.copy(rotationDeg = (currentRot + rotChange) % 360f)
                            )
                        },
                        onDragEnd = { onCommitLayerChange() }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.RotateRight,
                contentDescription = "Rotate",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
        Box(
            modifier = Modifier
                .width(1.5.dp)
                .height(12.dp)
                .background(StudioPrimary)
        )
    }

    // 4. Floating Action Pill
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF151924),
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .offset(y = 38.dp)
            .border(0.5.dp, Color(0xFF262C3E), RoundedCornerShape(18.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Quick Enhance button if layer is an image
            if (layer.type == LayerType.IMAGE) {
                IconButton(
                    onClick = onOpenQualityEnhance,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoFixHigh,
                        contentDescription = "Enhance Photo",
                        tint = StudioPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            IconButton(
                onClick = onDuplicate,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Duplicate",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            IconButton(
                onClick = onMoveUp,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = "Move Up",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            IconButton(
                onClick = onMoveDown,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = "Move Down",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            IconButton(
                onClick = onToggleLock,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = if (layer.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                    contentDescription = "Lock",
                    tint = if (layer.isLocked) StudioPrimary else Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFFF43F5E),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
