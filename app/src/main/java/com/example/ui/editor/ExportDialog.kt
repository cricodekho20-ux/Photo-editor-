package com.example.ui.editor

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.Project
import com.example.ui.theme.StudioPrimary
import com.example.util.ExportFormat
import com.example.util.ExportOptions

@Composable
fun ExportDialog(
    project: Project,
    onDismiss: () -> Unit,
    onExportSave: (ExportOptions) -> Unit,
    onExportShare: (ExportOptions) -> Unit
) {
    var selectedFormat by remember { mutableStateOf(ExportFormat.PNG) }
    var selectedScale by remember { mutableFloatStateOf(1.0f) }
    var qualityPercent by remember { mutableIntStateOf(100) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF181C26),
            tonalElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Export Artwork",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                    }
                }

                // Format selector (PNG vs JPG)
                Text("Image Format", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FormatCard(
                        format = "PNG",
                        subtitle = "Lossless, transparent",
                        isSelected = selectedFormat == ExportFormat.PNG,
                        onClick = { selectedFormat = ExportFormat.PNG },
                        modifier = Modifier.weight(1f)
                    )
                    FormatCard(
                        format = "JPG",
                        subtitle = "Compact, photo",
                        isSelected = selectedFormat == ExportFormat.JPEG,
                        onClick = { selectedFormat = ExportFormat.JPEG },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Resolution scale (1x vs 2x)
                Text("Resolution", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ResolutionCard(
                        title = "Standard 1x",
                        resolution = "${project.width} × ${project.height}",
                        isSelected = selectedScale == 1.0f,
                        onClick = { selectedScale = 1.0f },
                        modifier = Modifier.weight(1f)
                    )
                    ResolutionCard(
                        title = "Ultra HD 2x",
                        resolution = "${project.width * 2} × ${project.height * 2}",
                        isSelected = selectedScale == 2.0f,
                        onClick = { selectedScale = 2.0f },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Action buttons: Save to Gallery & Share
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val options = ExportOptions(
                                format = selectedFormat,
                                qualityPercent = qualityPercent,
                                scaleMultiplier = selectedScale
                            )
                            onExportSave(options)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("export_save_gallery_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save to Gallery", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val options = ExportOptions(
                                format = selectedFormat,
                                qualityPercent = qualityPercent,
                                scaleMultiplier = selectedScale
                            )
                            onExportShare(options)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("export_share_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share to Apps", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun FormatCard(
    format: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) StudioPrimary.copy(alpha = 0.15f) else Color(0xFF222736))
            .border(
                width = if (isSelected) 1.5.dp else 0.5.dp,
                color = if (isSelected) StudioPrimary else Color(0xFF334155),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = format,
                color = if (isSelected) StudioPrimary else Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun ResolutionCard(
    title: String,
    resolution: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) StudioPrimary.copy(alpha = 0.15f) else Color(0xFF222736))
            .border(
                width = if (isSelected) 1.5.dp else 0.5.dp,
                color = if (isSelected) StudioPrimary else Color(0xFF334155),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = title,
                color = if (isSelected) StudioPrimary else Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = resolution,
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )
        }
    }
}
