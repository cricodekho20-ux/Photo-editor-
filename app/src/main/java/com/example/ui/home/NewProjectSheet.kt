package com.example.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.StudioPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewProjectSheet(
    onDismiss: () -> Unit,
    onCreateProject: (title: String, width: Int, height: Int, ratioLabel: String, bgConfig: BackgroundConfig) -> Unit
) {
    var selectedPreset by remember { mutableStateOf<AspectRatioPreset?>(STANDARD_RATIO_PRESETS[0]) } // default 9:16
    var isCustomSize by remember { mutableStateOf(false) }

    var customWidthText by remember { mutableStateOf("1080") }
    var customHeightText by remember { mutableStateOf("1920") }
    var projectTitle by remember { mutableStateOf("My Design") }

    // Background preset selection
    var selectedBgType by remember { mutableStateOf(BackgroundType.SOLID) }
    var selectedSolidColor by remember { mutableLongStateOf(0xFF0F172A) }
    var selectedGradientColors by remember { mutableStateOf(listOf(0xFF1E1B4B, 0xFF4338CA)) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF181C26),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF475569)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Create New Design",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                }
            }

            // Project Name Field
            OutlinedTextField(
                value = projectTitle,
                onValueChange = { projectTitle = it },
                label = { Text("Project Name") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = StudioPrimary,
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedContainerColor = Color(0xFF131722),
                    unfocusedContainerColor = Color(0xFF131722)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Aspect Ratio Presets
            Text("Design Aspect Ratio", color = Color(0xFF94A3B8), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                STANDARD_RATIO_PRESETS.forEach { preset ->
                    val isSelected = !isCustomSize && selectedPreset?.label == preset.label
                    RatioPresetCard(
                        preset = preset,
                        isSelected = isSelected,
                        onClick = {
                            selectedPreset = preset
                            isCustomSize = false
                        }
                    )
                }

                // Custom Size Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isCustomSize) StudioPrimary.copy(alpha = 0.15f) else Color(0xFF222736),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isCustomSize) 1.5.dp else 0.5.dp,
                        color = if (isCustomSize) StudioPrimary else Color(0xFF334155)
                    ),
                    modifier = Modifier
                        .clickable { isCustomSize = true }
                        .size(width = 96.dp, height = 110.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AspectRatio,
                            contentDescription = "Custom",
                            tint = if (isCustomSize) StudioPrimary else Color(0xFF94A3B8),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Custom",
                            color = if (isCustomSize) StudioPrimary else Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "W × H",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Custom Size Inputs (if custom selected)
            if (isCustomSize) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = customWidthText,
                        onValueChange = { customWidthText = it.filter { char -> char.isDigit() } },
                        label = { Text("Width (px)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = StudioPrimary,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF131722),
                            unfocusedContainerColor = Color(0xFF131722)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = customHeightText,
                        onValueChange = { customHeightText = it.filter { char -> char.isDigit() } },
                        label = { Text("Height (px)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = StudioPrimary,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF131722),
                            unfocusedContainerColor = Color(0xFF131722)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Initial Background Picker
            Text("Initial Background", color = Color(0xFF94A3B8), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    BgSwatch(
                        title = "White",
                        background = Brush.linearGradient(listOf(Color.White, Color.White)),
                        isSelected = selectedBgType == BackgroundType.SOLID && selectedSolidColor == 0xFFFFFFFF,
                        onClick = {
                            selectedBgType = BackgroundType.SOLID
                            selectedSolidColor = 0xFFFFFFFF
                        }
                    )
                }
                item {
                    BgSwatch(
                        title = "Dark Studio",
                        background = Brush.linearGradient(listOf(Color(0xFF0F172A), Color(0xFF0F172A))),
                        isSelected = selectedBgType == BackgroundType.SOLID && selectedSolidColor == 0xFF0F172A,
                        onClick = {
                            selectedBgType = BackgroundType.SOLID
                            selectedSolidColor = 0xFF0F172A
                        }
                    )
                }
                item {
                    BgSwatch(
                        title = "Indigo Cyan",
                        background = Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF06B6D4))),
                        isSelected = selectedBgType == BackgroundType.GRADIENT && selectedGradientColors == listOf(0xFF6366F1, 0xFF06B6D4),
                        onClick = {
                            selectedBgType = BackgroundType.GRADIENT
                            selectedGradientColors = listOf(0xFF6366F1, 0xFF06B6D4)
                        }
                    )
                }
                item {
                    BgSwatch(
                        title = "Sunset Rose",
                        background = Brush.linearGradient(listOf(Color(0xFFF43F5E), Color(0xFFF59E0B))),
                        isSelected = selectedBgType == BackgroundType.GRADIENT && selectedGradientColors == listOf(0xFFF43F5E, 0xFFF59E0B),
                        onClick = {
                            selectedBgType = BackgroundType.GRADIENT
                            selectedGradientColors = listOf(0xFFF43F5E, 0xFFF59E0B)
                        }
                    )
                }
                item {
                    BgSwatch(
                        title = "Transparent",
                        background = Brush.linearGradient(listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8))),
                        isSelected = selectedBgType == BackgroundType.TRANSPARENT,
                        onClick = {
                            selectedBgType = BackgroundType.TRANSPARENT
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Start Editing Button
            Button(
                onClick = {
                    val finalWidth = if (isCustomSize) {
                        (customWidthText.toIntOrNull() ?: 1080).coerceIn(200, 4000)
                    } else {
                        selectedPreset?.width ?: 1080
                    }
                    val finalHeight = if (isCustomSize) {
                        (customHeightText.toIntOrNull() ?: 1920).coerceIn(200, 4000)
                    } else {
                        selectedPreset?.height ?: 1920
                    }
                    val ratioLabel = if (isCustomSize) "Custom" else (selectedPreset?.label ?: "1:1")

                    val bgConfig = when (selectedBgType) {
                        BackgroundType.SOLID -> BackgroundConfig(type = BackgroundType.SOLID, solidColor = selectedSolidColor)
                        BackgroundType.GRADIENT -> BackgroundConfig(type = BackgroundType.GRADIENT, gradientColors = selectedGradientColors)
                        BackgroundType.TRANSPARENT -> BackgroundConfig(type = BackgroundType.TRANSPARENT)
                        BackgroundType.IMAGE -> BackgroundConfig(type = BackgroundType.SOLID, solidColor = 0xFFFFFFFF)
                    }

                    onCreateProject(
                        projectTitle.ifBlank { "Untitled Project" },
                        finalWidth,
                        finalHeight,
                        ratioLabel,
                        bgConfig
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("create_canvas_button")
            ) {
                Icon(Icons.Default.Palette, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Open Editor",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun RatioPresetCard(
    preset: AspectRatioPreset,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) StudioPrimary.copy(alpha = 0.15f) else Color(0xFF222736),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 0.5.dp,
            color = if (isSelected) StudioPrimary else Color(0xFF334155)
        ),
        modifier = Modifier
            .clickable { onClick() }
            .size(width = 96.dp, height = 110.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = when (preset.label) {
                    "9:16" -> Icons.Default.StayCurrentPortrait
                    "1:1" -> Icons.Default.CropSquare
                    "4:3" -> Icons.Default.Portrait
                    "16:9" -> Icons.Default.StayCurrentLandscape
                    else -> Icons.Default.Crop
                },
                contentDescription = preset.label,
                tint = if (isSelected) StudioPrimary else Color(0xFF94A3B8),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = preset.label,
                color = if (isSelected) StudioPrimary else Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = preset.ratioDescription,
                color = Color(0xFF94A3B8),
                fontSize = 9.sp,
                maxLines = 1
            )
            Text(
                text = "${preset.width}x${preset.height}",
                color = Color(0xFF64748B),
                fontSize = 8.sp
            )
        }
    }
}

@Composable
private fun BgSwatch(
    title: String,
    background: Brush,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(background)
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) StudioPrimary else Color(0xFF475569),
                    shape = CircleShape
                )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(title, color = if (isSelected) StudioPrimary else Color(0xFF94A3B8), fontSize = 10.sp)
    }
}
