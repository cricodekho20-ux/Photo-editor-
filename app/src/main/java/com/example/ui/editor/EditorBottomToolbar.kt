package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StudioPrimary

data class ToolbarItem(
    val tool: ActiveEditorTool,
    val label: String,
    val icon: ImageVector,
    val testTag: String
)

val TOOLBAR_ITEMS = listOf(
    ToolbarItem(ActiveEditorTool.TEXT, "Text", Icons.Default.TextFields, "tool_text"),
    ToolbarItem(ActiveEditorTool.IMAGE, "Image", Icons.Default.AddPhotoAlternate, "tool_image"),
    ToolbarItem(ActiveEditorTool.ENHANCE, "Enhance ✨", Icons.Default.AutoFixHigh, "tool_enhance"),
    ToolbarItem(ActiveEditorTool.SHAPES, "Shapes", Icons.Default.Category, "tool_shapes"),
    ToolbarItem(ActiveEditorTool.STICKERS, "Stickers", Icons.Default.EmojiEmotions, "tool_stickers"),
    ToolbarItem(ActiveEditorTool.DRAW, "Draw", Icons.Default.Brush, "tool_draw"),
    ToolbarItem(ActiveEditorTool.BACKGROUND, "Background", Icons.Default.Palette, "tool_background"),
    ToolbarItem(ActiveEditorTool.LAYERS, "Layers", Icons.Default.Layers, "tool_layers")
)

@Composable
fun EditorBottomToolbar(
    activeTool: ActiveEditorTool,
    onToolClick: (ActiveEditorTool) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFF10131D),
        tonalElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF262C3E))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            for (item in TOOLBAR_ITEMS) {
                val isSelected = (activeTool == item.tool)

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) StudioPrimary.copy(alpha = 0.2f) else Color.Transparent)
                        .clickable { onToolClick(item.tool) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag(item.testTag),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) StudioPrimary else Color(0xFF94A3B8),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.label,
                        color = if (isSelected) StudioPrimary else Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}
