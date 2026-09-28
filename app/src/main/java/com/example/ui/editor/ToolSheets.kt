package com.example.ui.editor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.StudioPrimary
import com.example.util.FontHelper
import com.example.util.StickerCatalog
import kotlin.math.roundToInt

val COLOR_PALETTE = listOf(
    0xFFFFFFFF, 0xFF000000, 0xFFFFB300, 0xFF7C3AED, 0xFF06B6D4,
    0xFFF43F5E, 0xFF10B981, 0xFFA855F7, 0xFFEC4899, 0xFF3B82F6,
    0xFFEF4444, 0xFF84CC16, 0xFF14B8A6, 0xFFF97316, 0xFF64748B
)

val SHADOW_COLOR_PALETTE = listOf(
    0x00000000L, 0xB0000000L, 0xFF000000L, 0xCCFFB300L, 0xCC7C3AEDL,
    0xCC06B6D4L, 0xCCF43F5EL, 0xCC10B981L, 0xCCFFFFFFL
)

val GRADIENT_PRESETS = listOf(
    listOf(0xFF7C3AED, 0xFFFFB300),
    listOf(0xFFF43F5E, 0xFFFFB300),
    listOf(0xFF1E1B4B, 0xFF4338CA),
    listOf(0xFF0F172A, 0xFF1E293B),
    listOf(0xFF064E3B, 0xFF10B981),
    listOf(0xFF4A044E, 0xFFC026D3),
    listOf(0xFF7C2D12, 0xFFEA580C),
    listOf(0xFF0A0B10, 0xFF1A1E2C)
)

@Composable
fun EditorToolSheet(
    activeTool: ActiveEditorTool,
    selectedLayer: CanvasLayer?,
    allLayers: List<CanvasLayer>,
    backgroundConfig: BackgroundConfig,
    currentDrawTool: DrawToolType,
    currentDrawColor: Long,
    currentBrushSizePercent: Float,
    currentBrushOpacity: Float,
    onClose: () -> Unit,
    onAddText: (String, TextFontFamily, Long, Float, Boolean) -> Unit,
    onAddImage: (String) -> Unit,
    onOpenQualityEnhance: () -> Unit,
    onAddShape: (ShapeType, Long, Long, Float) -> Unit,
    onAddSticker: (String, String, Long) -> Unit,
    onUpdateSelectedLayer: ((CanvasLayer) -> CanvasLayer) -> Unit,
    onSelectLayer: (String?) -> Unit,
    onDeleteLayer: (String) -> Unit,
    onDuplicateLayer: (String) -> Unit,
    onToggleVisibility: (String) -> Unit,
    onToggleLock: (String) -> Unit,
    onMoveLayerUp: (String) -> Unit,
    onMoveLayerDown: (String) -> Unit,
    onSetDrawTool: (DrawToolType) -> Unit,
    onSetDrawColor: (Long) -> Unit,
    onSetBrushSize: (Float) -> Unit,
    onSetBrushOpacity: (Float) -> Unit,
    onClearStrokes: () -> Unit,
    onUpdateBackground: (BackgroundConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    if (activeTool == ActiveEditorTool.NONE) return

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 380.dp),
        color = Color(0xFF12151F),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        tonalElevation = 16.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF262C3E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            // Header with tool title and close button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (activeTool) {
                            ActiveEditorTool.TEXT -> Icons.Default.TextFields
                            ActiveEditorTool.IMAGE -> Icons.Default.AddPhotoAlternate
                            ActiveEditorTool.ENHANCE -> Icons.Default.AutoFixHigh
                            ActiveEditorTool.SHAPES -> Icons.Default.Category
                            ActiveEditorTool.STICKERS -> Icons.Default.EmojiEmotions
                            ActiveEditorTool.DRAW -> Icons.Default.Brush
                            ActiveEditorTool.BACKGROUND -> Icons.Default.Palette
                            ActiveEditorTool.LAYERS -> Icons.Default.Layers
                            ActiveEditorTool.NONE -> Icons.Default.Edit
                        },
                        contentDescription = null,
                        tint = StudioPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (activeTool) {
                            ActiveEditorTool.TEXT -> "Trending Typography (हिंदी & English)"
                            ActiveEditorTool.IMAGE -> "Photo & Image Studio"
                            ActiveEditorTool.ENHANCE -> "Photo Enhancer & Magic Studio"
                            ActiveEditorTool.SHAPES -> "Vector Shapes & Borders"
                            ActiveEditorTool.STICKERS -> "Sticker Catalog"
                            ActiveEditorTool.DRAW -> "Drawing & Freehand Brush"
                            ActiveEditorTool.BACKGROUND -> "Background Themes & Gradients"
                            ActiveEditorTool.LAYERS -> "Layer Management (${allLayers.size})"
                            ActiveEditorTool.NONE -> ""
                        },
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Tool",
                        tint = Color(0xFF94A3B8)
                    )
                }
            }

            Divider(color = Color(0xFF262C3E), thickness = 1.dp)

            // Body content per tool
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
            ) {
                when (activeTool) {
                    ActiveEditorTool.TEXT -> TextToolContent(
                        selectedLayer = selectedLayer,
                        onAddText = onAddText,
                        onUpdate = onUpdateSelectedLayer
                    )
                    ActiveEditorTool.IMAGE -> ImageToolContent(
                        selectedLayer = selectedLayer,
                        onAddImage = onAddImage,
                        onOpenQualityEnhance = onOpenQualityEnhance,
                        onUpdate = onUpdateSelectedLayer
                    )
                    ActiveEditorTool.ENHANCE -> EnhanceToolContent(
                        selectedLayer = selectedLayer,
                        onOpenQualityEnhance = onOpenQualityEnhance,
                        onUpdate = onUpdateSelectedLayer
                    )
                    ActiveEditorTool.SHAPES -> ShapesToolContent(
                        selectedLayer = selectedLayer,
                        onAddShape = onAddShape,
                        onUpdate = onUpdateSelectedLayer
                    )
                    ActiveEditorTool.STICKERS -> StickersToolContent(
                        selectedLayer = selectedLayer,
                        onAddSticker = onAddSticker,
                        onUpdate = onUpdateSelectedLayer
                    )
                    ActiveEditorTool.DRAW -> DrawToolContent(
                        currentDrawTool = currentDrawTool,
                        currentDrawColor = currentDrawColor,
                        currentBrushSizePercent = currentBrushSizePercent,
                        currentBrushOpacity = currentBrushOpacity,
                        onSetDrawTool = onSetDrawTool,
                        onSetDrawColor = onSetDrawColor,
                        onSetBrushSize = onSetBrushSize,
                        onSetBrushOpacity = onSetBrushOpacity,
                        onClearStrokes = onClearStrokes
                    )
                    ActiveEditorTool.BACKGROUND -> BackgroundToolContent(
                        config = backgroundConfig,
                        onUpdateBackground = onUpdateBackground
                    )
                    ActiveEditorTool.LAYERS -> LayersToolContent(
                        layers = allLayers,
                        selectedLayerId = selectedLayer?.id,
                        onSelectLayer = onSelectLayer,
                        onDeleteLayer = onDeleteLayer,
                        onDuplicateLayer = onDuplicateLayer,
                        onToggleVisibility = onToggleVisibility,
                        onToggleLock = onToggleLock,
                        onMoveLayerUp = onMoveLayerUp,
                        onMoveLayerDown = onMoveLayerDown
                    )
                    ActiveEditorTool.NONE -> {}
                }
            }
        }
    }
}

// ---------------- 1. TEXT TOOL ----------------

@Composable
private fun TextToolContent(
    selectedLayer: CanvasLayer?,
    onAddText: (String, TextFontFamily, Long, Float, Boolean) -> Unit,
    onUpdate: ((CanvasLayer) -> CanvasLayer) -> Unit
) {
    var textInput by remember(selectedLayer?.id, selectedLayer?.text) {
        mutableStateOf(selectedLayer?.text ?: "")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        OutlinedTextField(
            value = textInput,
            onValueChange = {
                textInput = it
                if (selectedLayer != null && selectedLayer.type == LayerType.TEXT) {
                    onUpdate { l -> l.copy(text = it) }
                }
            },
            placeholder = { Text("हिंदी या English में टेक्स्ट लिखें...") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = StudioPrimary,
                unfocusedBorderColor = Color(0xFF334155),
                focusedContainerColor = Color(0xFF0D0E15),
                unfocusedContainerColor = Color(0xFF0D0E15)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("text_input_field")
        )

        if (selectedLayer == null || selectedLayer.type != LayerType.TEXT) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val text = if (textInput.isNotBlank()) textInput else "PixCraft Text"
                        onAddText(text, TextFontFamily.HINDI_ROZHA, 0xFFFFFFFF, 34f, true)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Text", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                FilledTonalButton(
                    onClick = {
                        onAddText("शुभ दीपावली ✨", TextFontFamily.HINDI_ROZHA, 0xFFFFB300, 38f, true)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("हिंदी पोस्टर", fontWeight = FontWeight.Bold)
                }
            }
        }

        Text("🇮🇳 Trending Hindi Fonts", color = StudioPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val hindiFonts = listOf(
                Pair("Rozha One (बोल्ड पोस्टर)", TextFontFamily.HINDI_ROZHA),
                Pair("Kalam (कैलीग्राफी)", TextFontFamily.HINDI_KALAM),
                Pair("Yatra One (त्यौहार)", TextFontFamily.HINDI_YATRA),
                Pair("Modern Devanagari", TextFontFamily.MODERN_CLEAN)
            )
            items(hindiFonts) { (name, font) ->
                val isSelected = selectedLayer?.fontFamily == font
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (selectedLayer != null && selectedLayer.type == LayerType.TEXT) {
                            onUpdate { it.copy(fontFamily = font) }
                        }
                    },
                    label = { Text(name, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = StudioPrimary.copy(alpha = 0.25f),
                        selectedLabelColor = StudioPrimary
                    )
                )
            }
        }

        Text("✨ Trending English Fonts", color = Color(0xFF06B6D4), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val englishFonts = listOf(
                Pair("Bebas Neue (Heavy Title)", TextFontFamily.ENGLISH_BEBAS),
                Pair("Pacifico (Aesthetic Script)", TextFontFamily.ENGLISH_PACIFICO),
                Pair("Playfair Display (Luxury)", TextFontFamily.ENGLISH_PLAYFAIR),
                Pair("Cinzel (Royal Cinematic)", TextFontFamily.ENGLISH_CINZEL),
                Pair("Monospace", TextFontFamily.MONOSPACE),
                Pair("Serif Classic", TextFontFamily.SERIF),
                Pair("Casual Cursive", TextFontFamily.CURSIVE)
            )
            items(englishFonts) { (name, font) ->
                val isSelected = selectedLayer?.fontFamily == font
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (selectedLayer != null && selectedLayer.type == LayerType.TEXT) {
                            onUpdate { it.copy(fontFamily = font) }
                        }
                    },
                    label = { Text(name, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF06B6D4).copy(alpha = 0.25f),
                        selectedLabelColor = Color(0xFF06B6D4)
                    )
                )
            }
        }

        Text("Text Color", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(COLOR_PALETTE) { colorLong ->
                val isSelected = selectedLayer?.textColor == colorLong
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(colorLong), CircleShape)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) StudioPrimary else Color(0xFF475569),
                            shape = CircleShape
                        )
                        .clickable {
                            if (selectedLayer != null && selectedLayer.type == LayerType.TEXT) {
                                onUpdate { it.copy(textColor = colorLong) }
                            }
                        }
                )
            }
        }

        if (selectedLayer != null && selectedLayer.type == LayerType.TEXT) {
            // 🌟 1. HIGHLIGHT BORDER & STROKE SECTION
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF161A28),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF262C3E)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BorderColor, contentDescription = null, tint = StudioPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Highlight Border & Outline Stroke", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Text("Border / Stroke Color", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(COLOR_PALETTE) { col ->
                            val isSel = selectedLayer.strokeColor == col
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .background(Color(col), CircleShape)
                                    .border(if (isSel) 2.dp else 1.dp, if (isSel) StudioPrimary else Color(0xFF475569), CircleShape)
                                    .clickable { onUpdate { it.copy(strokeColor = col) } }
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Stroke Width", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text("${selectedLayer.strokeWidthDp.roundToInt()} dp", color = Color.White, fontSize = 11.sp)
                    }
                    Slider(
                        value = selectedLayer.strokeWidthDp,
                        onValueChange = { bw -> onUpdate { it.copy(strokeWidthDp = bw) } },
                        valueRange = 0f..15f
                    )

                    // Background Box / Pill Highlight
                    Text("Highlight Box / Pill Background", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(COLOR_PALETTE + listOf(0x00000000L)) { bgCol ->
                            val isSel = selectedLayer.backgroundColor == bgCol
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .background(if (bgCol == 0x00000000L) Color.Transparent else Color(bgCol), CircleShape)
                                    .border(if (isSel) 2.dp else 1.dp, if (isSel) StudioPrimary else Color(0xFF475569), CircleShape)
                                    .clickable { onUpdate { it.copy(backgroundColor = bgCol) } }
                            )
                        }
                    }
                }
            }

            // 🌟 2. SHADOW & GLOW (SHADAW) SECTION
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF161A28),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF262C3E)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BlurOn, contentDescription = null, tint = Color(0xFF06B6D4), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Shadow & Glow Effect (Shadaw)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Text("Shadow Color", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(SHADOW_COLOR_PALETTE) { shCol ->
                            val isSel = selectedLayer.shadowColor == shCol
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .background(if (shCol == 0x00000000L) Color.Transparent else Color(shCol), CircleShape)
                                    .border(if (isSel) 2.dp else 1.dp, if (isSel) StudioPrimary else Color(0xFF475569), CircleShape)
                                    .clickable { onUpdate { it.copy(shadowColor = shCol) } }
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Shadow Blur / Glow Radius", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text("${selectedLayer.shadowRadiusDp.roundToInt()} dp", color = Color.White, fontSize = 11.sp)
                    }
                    Slider(
                        value = selectedLayer.shadowRadiusDp,
                        onValueChange = { rad -> onUpdate { it.copy(shadowRadiusDp = rad) } },
                        valueRange = 0f..30f
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Shadow Offset Y", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text("${selectedLayer.shadowDyDp.roundToInt()} dp", color = Color.White, fontSize = 11.sp)
                    }
                    Slider(
                        value = selectedLayer.shadowDyDp,
                        onValueChange = { dy -> onUpdate { it.copy(shadowDyDp = dy) } },
                        valueRange = -25f..25f
                    )
                }
            }

            // Style Toggles: Bold, Italic, Alignment
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledIconToggleButton(
                        checked = selectedLayer.isBold,
                        onCheckedChange = { onUpdate { l -> l.copy(isBold = it) } }
                    ) {
                        Icon(Icons.Default.FormatBold, contentDescription = "Bold")
                    }
                    FilledIconToggleButton(
                        checked = selectedLayer.isItalic,
                        onCheckedChange = { onUpdate { l -> l.copy(isItalic = it) } }
                    ) {
                        Icon(Icons.Default.FormatItalic, contentDescription = "Italic")
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = { onUpdate { it.copy(textAlign = CanvasTextAlign.LEFT) } }) {
                        Icon(
                            Icons.Default.FormatAlignLeft,
                            contentDescription = "Left",
                            tint = if (selectedLayer.textAlign == CanvasTextAlign.LEFT) StudioPrimary else Color.White
                        )
                    }
                    IconButton(onClick = { onUpdate { it.copy(textAlign = CanvasTextAlign.CENTER) } }) {
                        Icon(
                            Icons.Default.FormatAlignCenter,
                            contentDescription = "Center",
                            tint = if (selectedLayer.textAlign == CanvasTextAlign.CENTER) StudioPrimary else Color.White
                        )
                    }
                    IconButton(onClick = { onUpdate { it.copy(textAlign = CanvasTextAlign.RIGHT) } }) {
                        Icon(
                            Icons.Default.FormatAlignRight,
                            contentDescription = "Right",
                            tint = if (selectedLayer.textAlign == CanvasTextAlign.RIGHT) StudioPrimary else Color.White
                        )
                    }
                }
            }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Font Size", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    Text("${selectedLayer.fontSizeSp.roundToInt()} sp", color = Color.White, fontSize = 12.sp)
                }
                Slider(
                    value = selectedLayer.fontSizeSp,
                    onValueChange = { size -> onUpdate { it.copy(fontSizeSp = size) } },
                    valueRange = 14f..90f
                )
            }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Opacity", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    Text("${(selectedLayer.opacity * 100).roundToInt()}%", color = Color.White, fontSize = 12.sp)
                }
                Slider(
                    value = selectedLayer.opacity,
                    onValueChange = { op -> onUpdate { it.copy(opacity = op) } },
                    valueRange = 0.1f..1.0f
                )
            }
        }
    }
}

// ---------------- 2. IMAGE TOOL ----------------

@Composable
private fun ImageToolContent(
    selectedLayer: CanvasLayer?,
    onAddImage: (String) -> Unit,
    onOpenQualityEnhance: () -> Unit,
    onUpdate: ((CanvasLayer) -> CanvasLayer) -> Unit
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            uri?.let { onAddImage(it.toString()) }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Button(
            onClick = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Choose Photo from Device", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        if (selectedLayer != null && selectedLayer.type == LayerType.IMAGE) {
            // High-Res Quality Enhancement Action Button
            Button(
                onClick = onOpenQualityEnhance,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("✨ Enhance Quality (Sharpen & Upscale 2x/4x)", color = Color.White, fontWeight = FontWeight.Bold)
            }

            // 🌟 1. HIGHLIGHT BORDER ON IMAGE
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF161A28),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF262C3E)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CropSquare, contentDescription = null, tint = StudioPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Highlight Border", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Text("Border Color", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(COLOR_PALETTE + listOf(0x00000000L)) { col ->
                            val isSel = selectedLayer.imageBorderColor == col
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .background(if (col == 0x00000000L) Color.Transparent else Color(col), CircleShape)
                                    .border(if (isSel) 2.dp else 1.dp, if (isSel) StudioPrimary else Color(0xFF475569), CircleShape)
                                    .clickable { onUpdate { it.copy(imageBorderColor = col) } }
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Border Width", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text("${selectedLayer.imageBorderWidthDp.roundToInt()} dp", color = Color.White, fontSize = 11.sp)
                    }
                    Slider(
                        value = selectedLayer.imageBorderWidthDp,
                        onValueChange = { bw -> onUpdate { it.copy(imageBorderWidthDp = bw) } },
                        valueRange = 0f..20f
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Corner Radius", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text("${selectedLayer.imageCornerRadiusDp.roundToInt()} dp", color = Color.White, fontSize = 11.sp)
                    }
                    Slider(
                        value = selectedLayer.imageCornerRadiusDp,
                        onValueChange = { cr -> onUpdate { it.copy(imageCornerRadiusDp = cr) } },
                        valueRange = 0f..60f
                    )
                }
            }

            // 🌟 2. SHADOW (SHADAW) & ELEVATION ON IMAGE
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF161A28),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF262C3E)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BlurOn, contentDescription = null, tint = Color(0xFF06B6D4), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Photo Shadow (Shadaw)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Text("Shadow Color", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(SHADOW_COLOR_PALETTE) { shCol ->
                            val isSel = selectedLayer.imageShadowColor == shCol
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .background(if (shCol == 0x00000000L) Color.Transparent else Color(shCol), CircleShape)
                                    .border(if (isSel) 2.dp else 1.dp, if (isSel) StudioPrimary else Color(0xFF475569), CircleShape)
                                    .clickable { onUpdate { it.copy(imageShadowColor = shCol) } }
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Shadow Blur / Glow", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text("${selectedLayer.imageShadowRadiusDp.roundToInt()} dp", color = Color.White, fontSize = 11.sp)
                    }
                    Slider(
                        value = selectedLayer.imageShadowRadiusDp,
                        onValueChange = { rad -> onUpdate { it.copy(imageShadowRadiusDp = rad) } },
                        valueRange = 0f..30f
                    )
                }
            }

            // Flip Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onUpdate { it.copy(flipHorizontal = !it.flipHorizontal) } },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Flip, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Flip H")
                }
                OutlinedButton(
                    onClick = { onUpdate { it.copy(flipVertical = !it.flipVertical) } },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.FlipCameraAndroid, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Flip V")
                }
            }
        }
    }
}

// ---------------- 3. PHOTO ENHANCER & MAGIC STUDIO ----------------

@Composable
private fun EnhanceToolContent(
    selectedLayer: CanvasLayer?,
    onOpenQualityEnhance: () -> Unit,
    onUpdate: ((CanvasLayer) -> CanvasLayer) -> Unit
) {
    if (selectedLayer == null || selectedLayer.type != LayerType.IMAGE) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = StudioPrimary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Select an image layer to enhance", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Tap on any photo in your design to apply AI super-resolution, sharpen details, or apply studio filters.", color = Color(0xFF94A3B8), fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
        return
    }

    val currentAdjustments = selectedLayer.adjustments

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // AI Super-Resolution & Detail Recovery Button
        Button(
            onClick = onOpenQualityEnhance,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("✨ AI Super-Resolution & Detail Recovery (2x/4x)", color = Color.White, fontWeight = FontWeight.Bold)
        }

        // 1-Tap Magic HD Enhance Button
        Button(
            onClick = {
                val newAuto = !currentAdjustments.isAutoEnhanced
                onUpdate {
                    it.copy(
                        adjustments = if (newAuto) {
                            it.adjustments.copy(
                                isAutoEnhanced = true,
                                filterPreset = PhotoFilterPreset.MAGIC_AUTO,
                                brightness = 0.08f,
                                contrast = 1.25f,
                                saturation = 1.35f
                            )
                        } else {
                            ImageAdjustments()
                        }
                    )
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (currentAdjustments.isAutoEnhanced) Color(0xFF06B6D4) else StudioPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (currentAdjustments.isAutoEnhanced) "HD Magic Enhance Active ✨" else "1-Tap Magic HD Enhance ✨",
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }

        // 1-Tap Preset Filters
        Text("Studio Color Filters", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val presets = listOf(
                Pair("Original", PhotoFilterPreset.NONE),
                Pair("Vivid Pop 🔥", PhotoFilterPreset.VIVID_POP),
                Pair("Golden Hour 🌅", PhotoFilterPreset.GOLDEN_HOUR),
                Pair("Cyber Neon ⚡", PhotoFilterPreset.CYBER_NEON),
                Pair("Noir B&W 🖤", PhotoFilterPreset.NOIR_BW),
                Pair("Vintage Sepia 📜", PhotoFilterPreset.VINTAGE_SEPIA),
                Pair("HDR Dramatic 🌌", PhotoFilterPreset.HDR_DRAMA),
                Pair("Film Matte 🎬", PhotoFilterPreset.FILM_MATTE)
            )
            items(presets) { (title, preset) ->
                val isSelected = currentAdjustments.filterPreset == preset
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        onUpdate {
                            it.copy(adjustments = it.adjustments.copy(filterPreset = preset, isAutoEnhanced = false))
                        }
                    },
                    label = { Text(title, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = StudioPrimary.copy(alpha = 0.25f),
                        selectedLabelColor = StudioPrimary
                    )
                )
            }
        }

        // Brightness Slider
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Brightness", color = Color(0xFF94A3B8), fontSize = 12.sp)
                Text("${(currentAdjustments.brightness * 100).roundToInt()}%", color = Color.White, fontSize = 12.sp)
            }
            Slider(
                value = currentAdjustments.brightness,
                onValueChange = { b ->
                    onUpdate { it.copy(adjustments = it.adjustments.copy(brightness = b)) }
                },
                valueRange = -0.5f..0.5f
            )
        }

        // Contrast Slider
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Contrast", color = Color(0xFF94A3B8), fontSize = 12.sp)
                Text("${((currentAdjustments.contrast - 1.0f) * 100).roundToInt()}%", color = Color.White, fontSize = 12.sp)
            }
            Slider(
                value = currentAdjustments.contrast,
                onValueChange = { c ->
                    onUpdate { it.copy(adjustments = it.adjustments.copy(contrast = c)) }
                },
                valueRange = 0.5f..1.8f
            )
        }

        // Saturation Slider
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Saturation / Color Vibrancy", color = Color(0xFF94A3B8), fontSize = 12.sp)
                Text("${((currentAdjustments.saturation) * 100).roundToInt()}%", color = Color.White, fontSize = 12.sp)
            }
            Slider(
                value = currentAdjustments.saturation,
                onValueChange = { s ->
                    onUpdate { it.copy(adjustments = it.adjustments.copy(saturation = s)) }
                },
                valueRange = 0.0f..2.2f
            )
        }

        // Warmth Slider
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Warmth / Temperature", color = Color(0xFF94A3B8), fontSize = 12.sp)
                Text("${(currentAdjustments.warmth * 100).roundToInt()}%", color = Color.White, fontSize = 12.sp)
            }
            Slider(
                value = currentAdjustments.warmth,
                onValueChange = { w ->
                    onUpdate { it.copy(adjustments = it.adjustments.copy(warmth = w)) }
                },
                valueRange = -0.5f..0.5f
            )
        }

        OutlinedButton(
            onClick = {
                onUpdate { it.copy(adjustments = ImageAdjustments()) }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reset Photo Adjustments")
        }
    }
}

// ---------------- 4. SHAPES TOOL ----------------

@Composable
private fun ShapesToolContent(
    selectedLayer: CanvasLayer?,
    onAddShape: (ShapeType, Long, Long, Float) -> Unit,
    onUpdate: ((CanvasLayer) -> CanvasLayer) -> Unit
) {
    val shapes = listOf(
        Pair("Rectangle", ShapeType.RECTANGLE),
        Pair("Rounded", ShapeType.ROUNDED_RECTANGLE),
        Pair("Circle", ShapeType.CIRCLE),
        Pair("Triangle", ShapeType.TRIANGLE),
        Pair("Line", ShapeType.LINE),
        Pair("Arrow", ShapeType.ARROW),
        Pair("Star", ShapeType.STAR),
        Pair("Heart", ShapeType.HEART),
        Pair("Hexagon", ShapeType.HEXAGON)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Add Shape", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(shapes) { (name, type) ->
                FilledTonalButton(
                    onClick = { onAddShape(type, 0xFF7C3AED, 0xFFFFB300, 0f) }
                ) {
                    Text(name, fontSize = 12.sp)
                }
            }
        }

        if (selectedLayer != null && selectedLayer.type == LayerType.SHAPE) {
            Text("Fill Color", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(COLOR_PALETTE + listOf(0x00000000L)) { colorLong ->
                    val isSelected = selectedLayer.fillColor == colorLong
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(if (colorLong == 0x00000000L) Color.Transparent else Color(colorLong), CircleShape)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) StudioPrimary else Color(0xFF475569),
                                shape = CircleShape
                            )
                            .clickable { onUpdate { it.copy(fillColor = colorLong) } }
                    )
                }
            }

            // 🌟 Highlight Border on Shape
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF161A28),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF262C3E)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Highlight Border", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    Text("Border Color", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(COLOR_PALETTE) { col ->
                            val isSel = selectedLayer.shapeBorderColor == col
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .background(Color(col), CircleShape)
                                    .border(if (isSel) 2.dp else 1.dp, if (isSel) StudioPrimary else Color(0xFF475569), CircleShape)
                                    .clickable { onUpdate { it.copy(shapeBorderColor = col) } }
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Border Width", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text("${selectedLayer.shapeBorderWidthDp.roundToInt()} dp", color = Color.White, fontSize = 11.sp)
                    }
                    Slider(
                        value = selectedLayer.shapeBorderWidthDp,
                        onValueChange = { bw -> onUpdate { it.copy(shapeBorderWidthDp = bw) } },
                        valueRange = 0f..25f
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Corner Radius", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text("${selectedLayer.shapeCornerRadiusDp.roundToInt()} dp", color = Color.White, fontSize = 11.sp)
                    }
                    Slider(
                        value = selectedLayer.shapeCornerRadiusDp,
                        onValueChange = { cr -> onUpdate { it.copy(shapeCornerRadiusDp = cr) } },
                        valueRange = 0f..50f
                    )
                }
            }

            // 🌟 Shape Shadow (Shadaw)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF161A28),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF262C3E)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Shape Shadow (Shadaw)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    Text("Shadow Color", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(SHADOW_COLOR_PALETTE) { shCol ->
                            val isSel = selectedLayer.shapeShadowColor == shCol
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .background(if (shCol == 0x00000000L) Color.Transparent else Color(shCol), CircleShape)
                                    .border(if (isSel) 2.dp else 1.dp, if (isSel) StudioPrimary else Color(0xFF475569), CircleShape)
                                    .clickable { onUpdate { it.copy(shapeShadowColor = shCol) } }
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Shadow Blur", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text("${selectedLayer.shapeShadowRadiusDp.roundToInt()} dp", color = Color.White, fontSize = 11.sp)
                    }
                    Slider(
                        value = selectedLayer.shapeShadowRadiusDp,
                        onValueChange = { rad -> onUpdate { it.copy(shapeShadowRadiusDp = rad) } },
                        valueRange = 0f..30f
                    )
                }
            }

            Text("Opacity", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Slider(
                value = selectedLayer.opacity,
                onValueChange = { op -> onUpdate { it.copy(opacity = op) } },
                valueRange = 0.1f..1.0f
            )
        }
    }
}

// ---------------- 5. STICKERS TOOL ----------------

@Composable
private fun StickersToolContent(
    selectedLayer: CanvasLayer?,
    onAddSticker: (String, String, Long) -> Unit,
    onUpdate: ((CanvasLayer) -> CanvasLayer) -> Unit
) {
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    val categories = StickerCatalog.categories
    val activeCategory = categories[selectedCategoryIndex]

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedCategoryIndex,
            edgePadding = 8.dp,
            containerColor = Color.Transparent,
            contentColor = StudioPrimary,
            divider = {}
        ) {
            categories.forEachIndexed { index, cat ->
                Tab(
                    selected = selectedCategoryIndex == index,
                    onClick = { selectedCategoryIndex = index },
                    text = { Text("${cat.icon} ${cat.name}", fontSize = 12.sp) }
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 60.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 160.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(activeCategory.stickers) { sticker ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1A1E2C),
                    modifier = Modifier
                        .clickable {
                            onAddSticker(activeCategory.name, sticker.symbol, sticker.defaultColor)
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = sticker.symbol,
                            fontSize = 20.sp,
                            color = Color(sticker.defaultColor)
                        )
                    }
                }
            }
        }

        if (selectedLayer != null && selectedLayer.type == LayerType.STICKER) {
            // Sticker Shadow (Shadaw)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF161A28),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF262C3E)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Sticker Shadow (Shadaw)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    Text("Shadow Color", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(SHADOW_COLOR_PALETTE) { shCol ->
                            val isSel = selectedLayer.stickerShadowColor == shCol
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .background(if (shCol == 0x00000000L) Color.Transparent else Color(shCol), CircleShape)
                                    .border(if (isSel) 2.dp else 1.dp, if (isSel) StudioPrimary else Color(0xFF475569), CircleShape)
                                    .clickable { onUpdate { it.copy(stickerShadowColor = shCol) } }
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Shadow Blur", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text("${selectedLayer.stickerShadowRadiusDp.roundToInt()} dp", color = Color.White, fontSize = 11.sp)
                    }
                    Slider(
                        value = selectedLayer.stickerShadowRadiusDp,
                        onValueChange = { rad -> onUpdate { it.copy(stickerShadowRadiusDp = rad) } },
                        valueRange = 0f..30f
                    )
                }
            }
        }
    }
}

// ---------------- 6. DRAW TOOL ----------------

@Composable
private fun DrawToolContent(
    currentDrawTool: DrawToolType,
    currentDrawColor: Long,
    currentBrushSizePercent: Float,
    currentBrushOpacity: Float,
    onSetDrawTool: (DrawToolType) -> Unit,
    onSetDrawColor: (Long) -> Unit,
    onSetBrushSize: (Float) -> Unit,
    onSetBrushOpacity: (Float) -> Unit,
    onClearStrokes: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val tools = listOf(
                Pair("Pen", DrawToolType.PEN),
                Pair("Brush", DrawToolType.BRUSH),
                Pair("Marker", DrawToolType.MARKER),
                Pair("Highlight", DrawToolType.HIGHLIGHTER),
                Pair("Eraser", DrawToolType.ERASER)
            )
            tools.forEach { (name, tool) ->
                val isSelected = currentDrawTool == tool
                FilterChip(
                    selected = isSelected,
                    onClick = { onSetDrawTool(tool) },
                    label = { Text(name, fontSize = 11.sp) }
                )
            }
        }

        if (currentDrawTool != DrawToolType.ERASER) {
            Text("Ink Color", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(COLOR_PALETTE) { colorLong ->
                    val isSelected = currentDrawColor == colorLong
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(Color(colorLong), CircleShape)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) StudioPrimary else Color(0xFF475569),
                                shape = CircleShape
                            )
                            .clickable { onSetDrawColor(colorLong) }
                    )
                }
            }
        }

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Brush Size", color = Color(0xFF94A3B8), fontSize = 12.sp)
                Text("${(currentBrushSizePercent * 1000).roundToInt()} px", color = Color.White, fontSize = 12.sp)
            }
            Slider(
                value = currentBrushSizePercent,
                onValueChange = { onSetBrushSize(it) },
                valueRange = 0.005f..0.06f
            )
        }

        OutlinedButton(
            onClick = onClearStrokes,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF43F5E)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.DeleteOutline, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Clear Drawing Strokes")
        }
    }
}

// ---------------- 7. BACKGROUND TOOL ----------------

@Composable
private fun BackgroundToolContent(
    config: BackgroundConfig,
    onUpdateBackground: (BackgroundConfig) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            uri?.let {
                onUpdateBackground(config.copy(type = BackgroundType.IMAGE, imageUri = it.toString()))
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = StudioPrimary
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Solid") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Gradient") })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Image") })
            Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Transparent") })
        }

        when (selectedTab) {
            0 -> {
                Text("Solid Background Color", color = Color(0xFF94A3B8), fontSize = 12.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(COLOR_PALETTE) { colorLong ->
                        val isSelected = config.type == BackgroundType.SOLID && config.solidColor == colorLong
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color(colorLong), CircleShape)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) StudioPrimary else Color(0xFF475569),
                                    shape = CircleShape
                                )
                                .clickable {
                                    onUpdateBackground(
                                        config.copy(type = BackgroundType.SOLID, solidColor = colorLong)
                                    )
                                }
                        )
                    }
                }
            }
            1 -> {
                Text("Cyber & Sunset Gradients", color = Color(0xFF94A3B8), fontSize = 12.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(GRADIENT_PRESETS) { gradient ->
                        Box(
                            modifier = Modifier
                                .size(50.dp, 34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(gradient.map { Color(it) })
                                )
                                .clickable {
                                    onUpdateBackground(
                                        config.copy(
                                            type = BackgroundType.GRADIENT,
                                            gradientColors = gradient
                                        )
                                    )
                                }
                        )
                    }
                }

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Gradient Angle", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text("${config.gradientAngle.roundToInt()}°", color = Color.White, fontSize = 12.sp)
                    }
                    Slider(
                        value = config.gradientAngle,
                        onValueChange = { angle ->
                            onUpdateBackground(config.copy(gradientAngle = angle))
                        },
                        valueRange = 0f..360f
                    )
                }
            }
            2 -> {
                Button(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Background Image", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
            3 -> {
                Button(
                    onClick = {
                        onUpdateBackground(config.copy(type = BackgroundType.TRANSPARENT))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.GridOn, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Set Transparent (Checkerboard)", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ---------------- 8. LAYERS TOOL ----------------

@Composable
private fun LayersToolContent(
    layers: List<CanvasLayer>,
    selectedLayerId: String?,
    onSelectLayer: (String?) -> Unit,
    onDeleteLayer: (String) -> Unit,
    onDuplicateLayer: (String) -> Unit,
    onToggleVisibility: (String) -> Unit,
    onToggleLock: (String) -> Unit,
    onMoveLayerUp: (String) -> Unit,
    onMoveLayerDown: (String) -> Unit
) {
    if (layers.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No layers added yet. Add text, images, or shapes!", color = Color(0xFF94A3B8), fontSize = 13.sp)
        }
        return
    }

    val reversedLayers = remember(layers) { layers.reversed() }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(reversedLayers, key = { it.id }) { layer ->
            val isSelected = layer.id == selectedLayerId

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) Color(0xFF1E2435) else Color(0xFF151924),
                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, StudioPrimary) else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectLayer(layer.id) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = when (layer.type) {
                                LayerType.TEXT -> Icons.Default.TextFields
                                LayerType.IMAGE -> Icons.Default.Image
                                LayerType.SHAPE -> Icons.Default.Category
                                LayerType.STICKER -> Icons.Default.EmojiEmotions
                            },
                            contentDescription = null,
                            tint = if (isSelected) StudioPrimary else Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = layer.name,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        IconButton(
                            onClick = { onMoveLayerUp(layer.id) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Up", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                        }
                        IconButton(
                            onClick = { onMoveLayerDown(layer.id) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Down", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                        }
                        IconButton(
                            onClick = { onToggleVisibility(layer.id) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (layer.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Visibility",
                                tint = if (layer.isVisible) Color.White else Color(0xFF64748B),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = { onToggleLock(layer.id) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (layer.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = "Lock",
                                tint = if (layer.isLocked) StudioPrimary else Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = { onDeleteLayer(layer.id) },
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
        }
    }
}
