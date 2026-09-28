package com.example.util

import android.content.Context
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.*

enum class UpscaleFactor(val multiplier: Int, val label: String) {
    ORIGINAL(1, "Original (1x)"),
    UPSCALE_2X(2, "2x HD Upscale"),
    UPSCALE_4X(4, "4x Ultra HD")
}

data class EnhancementConfig(
    val strengthPercent: Int = 80, // 0 .. 100
    val upscaleFactor: UpscaleFactor = UpscaleFactor.UPSCALE_2X,
    val noiseReductionPercent: Int = 40,
    val smartSharpenPercent: Int = 75,
    val textureBoostPercent: Int = 60,
    val naturalColorEnhance: Boolean = true
)

interface SuperResolutionProcessor {
    suspend fun enhance(context: Context, sourceBitmap: Bitmap, config: EnhancementConfig): Bitmap
}

object PhotoQualityEnhancer : SuperResolutionProcessor {

    /**
     * Safely loads a bitmap from any URI (content://, file://, resource, or local file path)
     * with auto-orientation correction and memory-safe downsampling.
     */
    suspend fun loadBitmapSafely(
        context: Context,
        uriString: String,
        maxDimension: Int = 2048
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val uri = Uri.parse(uriString)
            fun openStream(): InputStream? {
                return when {
                    uriString.startsWith("file://") -> {
                        val path = uri.path ?: uriString.removePrefix("file://")
                        File(path).inputStream()
                    }
                    uriString.startsWith("/") -> {
                        File(uriString).inputStream()
                    }
                    else -> {
                        context.contentResolver.openInputStream(uri)
                    }
                }
            }

            // 1. Decode bounds first to prevent OutOfMemoryError
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            var stream = openStream() ?: return@withContext null
            BitmapFactory.decodeStream(stream, null, options)
            stream.close()

            if (options.outWidth <= 0 || options.outHeight <= 0) {
                return@withContext null
            }

            var sampleSize = 1
            while (options.outWidth / (sampleSize * 2) >= maxDimension ||
                options.outHeight / (sampleSize * 2) >= maxDimension
            ) {
                sampleSize *= 2
            }

            // 2. Decode sampled bitmap
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inMutable = true
            }
            stream = openStream() ?: return@withContext null
            var bitmap = BitmapFactory.decodeStream(stream, null, decodeOptions)
            stream.close()

            if (bitmap == null) return@withContext null

            // 3. Auto-orient from EXIF if content or file URI
            try {
                val exifStream = openStream()
                if (exifStream != null) {
                    val exif = ExifInterface(exifStream)
                    val orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                    exifStream.close()

                    val rotation = when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                        else -> 0f
                    }

                    if (rotation != 0f) {
                        val matrix = Matrix().apply { postRotate(rotation) }
                        val rotated = Bitmap.createBitmap(
                            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                        )
                        if (rotated != bitmap) {
                            bitmap.recycle()
                            bitmap = rotated
                        }
                    }
                }
            } catch (_: Exception) {}

            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun enhance(
        context: Context,
        sourceBitmap: Bitmap,
        config: EnhancementConfig
    ): Bitmap = withContext(Dispatchers.Default) {
        try {
            processEnhancement(sourceBitmap, config)
        } catch (oom: OutOfMemoryError) {
            // Fallback to 1x upscale if memory is constrained
            if (config.upscaleFactor != UpscaleFactor.ORIGINAL) {
                val fallbackConfig = config.copy(upscaleFactor = UpscaleFactor.ORIGINAL)
                processEnhancement(sourceBitmap, fallbackConfig)
            } else {
                sourceBitmap.copy(Bitmap.Config.ARGB_8888, true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            sourceBitmap.copy(Bitmap.Config.ARGB_8888, true)
        }
    }

    private fun processEnhancement(sourceBitmap: Bitmap, config: EnhancementConfig): Bitmap {
        val multiplier = config.upscaleFactor.multiplier
        val targetWidth = (sourceBitmap.width * multiplier).coerceIn(50, 4096)
        val targetHeight = (sourceBitmap.height * multiplier).coerceIn(50, 4096)

        // 1. High Quality Scaled Base Bitmap with Anti-Aliasing and Bilinear Interpolation
        val scaledBitmap = if (multiplier > 1) {
            val highQualityPaint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG).apply {
                isDither = true
            }
            val upscaled = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(upscaled)
            val srcRect = Rect(0, 0, sourceBitmap.width, sourceBitmap.height)
            val dstRect = Rect(0, 0, targetWidth, targetHeight)
            canvas.drawBitmap(sourceBitmap, srcRect, dstRect, highQualityPaint)
            upscaled
        } else {
            sourceBitmap.copy(Bitmap.Config.ARGB_8888, true)
        }

        val w = scaledBitmap.width
        val h = scaledBitmap.height
        val pixels = IntArray(w * h)
        scaledBitmap.getPixels(pixels, 0, w, 0, 0, w, h)

        val strengthNorm = (config.strengthPercent / 100f).coerceIn(0f, 1f)
        val sharpenNorm = (config.smartSharpenPercent / 100f) * strengthNorm
        val noiseNorm = (config.noiseReductionPercent / 100f) * strengthNorm
        val textureNorm = (config.textureBoostPercent / 100f) * strengthNorm

        // 2. Multi-Frequency Luminance & Detail Map Extraction
        val outputPixels = IntArray(w * h)
        val luminances = FloatArray(w * h)

        for (i in pixels.indices) {
            val c = pixels[i]
            val r = (c shr 16) and 0xFF
            val g = (c shr 8) and 0xFF
            val b = c and 0xFF
            luminances[i] = 0.299f * r + 0.587f * g + 0.114f * b
        }

        // Multi-Scale Adaptive Edge & High-Frequency Detail Sharpener
        val sharpenWeight = (1.1f * sharpenNorm + 0.75f * textureNorm).coerceIn(0f, 3.0f)
        val noiseThreshold = 12f * (1f - noiseNorm * 0.5f)

        for (y in 1 until h - 1) {
            val rowOffset = y * w
            for (x in 1 until w - 1) {
                val idx = rowOffset + x
                val centerLum = luminances[idx]

                val lUp = luminances[idx - w]
                val lDown = luminances[idx + w]
                val lLeft = luminances[idx - 1]
                val lRight = luminances[idx + 1]

                // Diagonal neighbors for omnidirectional sharpness
                val lUpLeft = luminances[idx - w - 1]
                val lUpRight = luminances[idx - w + 1]
                val lDownLeft = luminances[idx + w - 1]
                val lDownRight = luminances[idx + w + 1]

                val directAvg = (lUp + lDown + lLeft + lRight) * 0.2f
                val diagAvg = (lUpLeft + lUpRight + lDownLeft + lDownRight) * 0.05f
                val avgNeighborLum = directAvg + diagAvg

                val diff = centerLum - avgNeighborLum
                val c = pixels[idx]
                val a = (c shr 24) and 0xFF
                var r = (c shr 16) and 0xFF
                var g = (c shr 8) and 0xFF
                var b = c and 0xFF

                if (abs(diff) < noiseThreshold && noiseNorm > 0.08f) {
                    // Adaptive Bilateral Denoising on flat textures & skin
                    val blend = noiseNorm * 0.48f
                    val smoothLum = centerLum * (1f - blend) + avgNeighborLum * blend
                    val lumRatio = if (centerLum > 0.01f) smoothLum / centerLum else 1f
                    r = (r * lumRatio).roundToInt().coerceIn(0, 255)
                    g = (g * lumRatio).roundToInt().coerceIn(0, 255)
                    b = (b * lumRatio).roundToInt().coerceIn(0, 255)
                } else if (sharpenWeight > 0.02f) {
                    // Non-linear halo-clamped edge boost (sharpens eyes, contours, hair, textures)
                    val clampedDelta = diff.coerceIn(-50f, 50f) * sharpenWeight
                    r = (r + clampedDelta * 1.2f).roundToInt().coerceIn(0, 255)
                    g = (g + clampedDelta * 1.2f).roundToInt().coerceIn(0, 255)
                    b = (b + clampedDelta * 1.2f).roundToInt().coerceIn(0, 255)
                }

                outputPixels[idx] = (a shl 24) or (r shl 16) or (g shl 8) or b
            }
        }

        // Fill borders
        for (x in 0 until w) {
            outputPixels[x] = pixels[x]
            outputPixels[(h - 1) * w + x] = pixels[(h - 1) * w + x]
        }
        for (y in 0 until h) {
            outputPixels[y * w] = pixels[y * w]
            outputPixels[y * w + (w - 1)] = pixels[y * w + (w - 1)]
        }

        val enhancedBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        enhancedBitmap.setPixels(outputPixels, 0, w, 0, 0, w, h)

        // 3. Dynamic Range, Natural Clarity & Color Vibrancy
        if (config.naturalColorEnhance && strengthNorm > 0.05f) {
            val colorCanvas = Canvas(enhancedBitmap)
            val colorPaint = Paint(Paint.ANTI_ALIAS_FLAG)

            val colorMatrix = ColorMatrix()
            val satMatrix = ColorMatrix().apply { setSaturation(1f + 0.25f * strengthNorm) }
            val contrastMatrix = ColorMatrix(
                floatArrayOf(
                    1f + 0.14f * strengthNorm, 0f, 0f, 0f, 8f * strengthNorm,
                    0f, 1f + 0.14f * strengthNorm, 0f, 0f, 8f * strengthNorm,
                    0f, 0f, 1f + 0.14f * strengthNorm, 0f, 8f * strengthNorm,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            colorMatrix.postConcat(satMatrix)
            colorMatrix.postConcat(contrastMatrix)

            colorPaint.colorFilter = ColorMatrixColorFilter(colorMatrix)
            colorCanvas.drawBitmap(enhancedBitmap, 0f, 0f, colorPaint)
        }

        return enhancedBitmap
    }

    suspend fun saveEnhancedBitmapToCache(
        context: Context,
        bitmap: Bitmap,
        title: String = "Enhanced_Photo"
    ): Uri = withContext(Dispatchers.IO) {
        val cacheDir = File(context.filesDir, "enhanced_photos")
        if (!cacheDir.exists()) cacheDir.mkdirs()

        val safeTitle = title.replace("[^a-zA-Z0-9_]".toRegex(), "_")
        val file = File(cacheDir, "${safeTitle}_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        Uri.fromFile(file)
    }
}
