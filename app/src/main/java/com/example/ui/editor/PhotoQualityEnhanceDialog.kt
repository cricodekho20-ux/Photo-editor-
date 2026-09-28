package com.example.ui.editor

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.roundToInt
import com.example.model.CanvasLayer
import com.example.ui.theme.StudioPrimary
import com.example.util.EnhancementConfig
import com.example.util.PhotoQualityEnhancer
import com.example.util.UpscaleFactor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoQualityEnhanceDialog(
    layer: CanvasLayer,
    onDismiss: () -> Unit,
    onApplyEnhancedLayer: (enhancedUri: String, newWidthPx: Int, newHeightPx: Int) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var enhancedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoadingOriginal by remember { mutableStateOf(true) }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Configuration states
    var strengthPercent by remember { mutableIntStateOf(80) }
    var selectedUpscale by remember { mutableStateOf(UpscaleFactor.UPSCALE_2X) }
    var smartSharpenPercent by remember { mutableIntStateOf(75) }
    var noiseReductionPercent by remember { mutableIntStateOf(40) }
    var textureBoostPercent by remember { mutableIntStateOf(60) }
    var naturalColorEnhance by remember { mutableStateOf(true) }

    // Comparison view mode: false = Enhanced, true = Original
    var showOriginalPreview by remember { mutableStateOf(false) }
    var debounceJob by remember { mutableStateOf<Job?>(null) }

    // Safe Bitmap Loader
    LaunchedEffect(layer.imageUri) {
        isLoadingOriginal = true
        errorMessage = null
        if (!layer.imageUri.isNullOrBlank()) {
            val bmp = PhotoQualityEnhancer.loadBitmapSafely(context, layer.imageUri)
            if (bmp != null) {
                originalBitmap = bmp
                isLoadingOriginal = false
            } else {
                isLoadingOriginal = false
                errorMessage = "Could not load image. Please select a valid photo."
            }
        } else {
            isLoadingOriginal = false
            errorMessage = "No photo selected."
        }
    }

    fun runEnhancement() {
        val src = originalBitmap ?: return
        coroutineScope.launch {
            isProcessing = true
            val config = EnhancementConfig(
                strengthPercent = strengthPercent,
                upscaleFactor = selectedUpscale,
                noiseReductionPercent = noiseReductionPercent,
                smartSharpenPercent = smartSharpenPercent,
                textureBoostPercent = textureBoostPercent,
                naturalColorEnhance = naturalColorEnhance
            )
            val result = PhotoQualityEnhancer.enhance(context, src, config)
            enhancedBitmap = result
            isProcessing = false
        }
    }

    // Auto-update with debounce on parameter change
    LaunchedEffect(
        originalBitmap,
        selectedUpscale,
        strengthPercent,
        smartSharpenPercent,
        noiseReductionPercent,
        textureBoostPercent,
        naturalColorEnhance
    ) {
        if (originalBitmap != null) {
            debounceJob?.cancel()
            debounceJob = coroutineScope.launch {
                delay(180) // Smooth debounce
                runEnhancement()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f)
                .padding(6.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF10131D),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF262C3E)),
            tonalElevation = 24.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(StudioPrimary.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoFixHigh,
                                contentDescription = null,
                                tint = StudioPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Photo Quality Enhancer",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "AI Sharpening • Super-Resolution (2x/4x) • Clarity",
                                color = StudioPrimary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Preview Area (Original vs Enhanced)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF07080D))
                        .border(1.dp, Color(0xFF1E2436), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoadingOriginal) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = StudioPrimary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Loading Photo...", color = Color.White, fontSize = 12.sp)
                        }
                    } else if (errorMessage != null) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(errorMessage ?: "Error", color = Color.White, fontSize = 13.sp)
                        }
                    } else {
                        val activeBmp = if (showOriginalPreview) originalBitmap else (enhancedBitmap ?: originalBitmap)

                        if (activeBmp != null) {
                            Image(
                                bitmap = activeBmp.asImageBitmap(),
                                contentDescription = "Preview",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        if (isProcessing) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Black.copy(alpha = 0.8f),
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = StudioPrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Enhancing details...", color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }

                        // Original | Enhanced Toggle
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF161A28).copy(alpha = 0.92f),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF334155)),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (showOriginalPreview) Color(0xFF334155) else Color.Transparent,
                                    modifier = Modifier
                                        .clickable { showOriginalPreview = true }
                                        .testTag("preview_original_toggle")
                                ) {
                                    Text(
                                        text = "Original",
                                        color = if (showOriginalPreview) Color.White else Color(0xFF94A3B8),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (!showOriginalPreview) StudioPrimary else Color.Transparent,
                                    modifier = Modifier
                                        .clickable { showOriginalPreview = false }
                                        .testTag("preview_enhanced_toggle")
                                ) {
                                    Text(
                                        text = "Enhanced ✨",
                                        color = if (!showOriginalPreview) Color.Black else Color(0xFF94A3B8),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        // Dimension Badge
                        val currentW = if (showOriginalPreview) (originalBitmap?.width ?: 0) else (enhancedBitmap?.width ?: (originalBitmap?.width ?: 0))
                        val currentH = if (showOriginalPreview) (originalBitmap?.height ?: 0) else (enhancedBitmap?.height ?: (originalBitmap?.height ?: 0))
                        val badgeLabel = if (showOriginalPreview) "Original: ${currentW}×${currentH}px" else "${selectedUpscale.label}: ${currentW}×${currentH}px"
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.Black.copy(alpha = 0.75f),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = badgeLabel,
                                color = StudioPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Controls
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Enhancement Strength (0% -> 100%)
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Enhancement Strength", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("$strengthPercent%", color = StudioPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = strengthPercent.toFloat(),
                            onValueChange = { strengthPercent = it.roundToInt() },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = StudioPrimary,
                                activeTrackColor = StudioPrimary
                            )
                        )
                    }

                    // 2. Upscale (Original, 2x, 4x)
                    Text("Resolution Upscale", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UpscaleFactor.entries.forEach { factor ->
                            val isSelected = selectedUpscale == factor
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) StudioPrimary.copy(alpha = 0.15f) else Color(0xFF151924),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 1.5.dp else 0.5.dp,
                                    color = if (isSelected) StudioPrimary else Color(0xFF262C3E)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedUpscale = factor }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = factor.label,
                                        color = if (isSelected) StudioPrimary else Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = when (factor) {
                                            UpscaleFactor.ORIGINAL -> "Fast Clarity"
                                            UpscaleFactor.UPSCALE_2X -> "Crisp HD"
                                            UpscaleFactor.UPSCALE_4X -> "Ultra Detail"
                                        },
                                        color = Color(0xFF94A3B8),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    // 3. Fine-tuning adjustments
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF151924), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Fine Detail Recovery", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)

                        // Smart Sharpen
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Smart Sharpening", color = Color.White, fontSize = 12.sp)
                            Text("$smartSharpenPercent%", color = StudioPrimary, fontSize = 12.sp)
                        }
                        Slider(
                            value = smartSharpenPercent.toFloat(),
                            onValueChange = { smartSharpenPercent = it.roundToInt() },
                            valueRange = 0f..100f
                        )

                        // Noise Reduction
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Noise & Grain Reduction", color = Color.White, fontSize = 12.sp)
                            Text("$noiseReductionPercent%", color = StudioPrimary, fontSize = 12.sp)
                        }
                        Slider(
                            value = noiseReductionPercent.toFloat(),
                            onValueChange = { noiseReductionPercent = it.roundToInt() },
                            valueRange = 0f..100f
                        )

                        // Texture Boost
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Texture & Clarity Boost", color = Color.White, fontSize = 12.sp)
                            Text("$textureBoostPercent%", color = StudioPrimary, fontSize = 12.sp)
                        }
                        Slider(
                            value = textureBoostPercent.toFloat(),
                            onValueChange = { textureBoostPercent = it.roundToInt() },
                            valueRange = 0f..100f
                        )

                        // Natural Color & Dynamic Range Recovery
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Natural Color & Contrast Recovery", color = Color.White, fontSize = 12.sp)
                                Text("Restores dynamic range without clipping", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            }
                            Switch(
                                checked = naturalColorEnhance,
                                onCheckedChange = { naturalColorEnhance = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = StudioPrimary
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Action Buttons: Cancel, Preview, Apply
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", color = Color(0xFF94A3B8))
                    }

                    FilledTonalButton(
                        onClick = { runEnhancement() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Preview")
                    }

                    Button(
                        onClick = {
                            val enhanced = enhancedBitmap ?: originalBitmap ?: return@Button
                            coroutineScope.launch {
                                val savedUri = PhotoQualityEnhancer.saveEnhancedBitmapToCache(
                                    context,
                                    enhanced,
                                    "Enhanced_${layer.name.replace(" ", "_")}"
                                )
                                onApplyEnhancedLayer(savedUri.toString(), enhanced.width, enhanced.height)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.8f)
                            .testTag("apply_enhanced_layer_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Apply ✨",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
