package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.cos
import kotlin.math.sin

enum class ExportFormat {
    PNG,
    JPEG
}

data class ExportOptions(
    val format: ExportFormat = ExportFormat.PNG,
    val qualityPercent: Int = 100,
    val scaleMultiplier: Float = 1.0f
)

object CanvasExporter {

    suspend fun renderProjectToBitmap(
        context: Context,
        project: Project,
        scaleMultiplier: Float = 1.0f
    ): Bitmap = withContext(Dispatchers.IO) {
        val targetWidth = (project.width * scaleMultiplier).toInt().coerceAtLeast(100)
        val targetHeight = (project.height * scaleMultiplier).toInt().coerceAtLeast(100)

        val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Draw Background
        drawBackground(context, canvas, targetWidth.toFloat(), targetHeight.toFloat(), project.backgroundConfig)

        // 2. Draw Visible Layers
        for (layer in project.layers) {
            if (!layer.isVisible) continue
            drawLayer(context, canvas, layer, targetWidth.toFloat(), targetHeight.toFloat(), scaleMultiplier)
        }

        // 3. Draw Strokes
        for (stroke in project.drawStrokes) {
            drawStroke(canvas, stroke, targetWidth.toFloat(), targetHeight.toFloat(), scaleMultiplier)
        }

        bitmap
    }

    private fun drawBackground(
        context: Context,
        canvas: Canvas,
        width: Float,
        height: Float,
        config: BackgroundConfig
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        when (config.type) {
            BackgroundType.SOLID -> {
                paint.color = config.solidColor.toInt()
                canvas.drawRect(0f, 0f, width, height, paint)
            }
            BackgroundType.GRADIENT -> {
                val colors = if (config.gradientColors.isNotEmpty()) {
                    config.gradientColors.map { it.toInt() }.toIntArray()
                } else {
                    intArrayOf(0xFF7C3AED.toInt(), 0xFFFFB300.toInt())
                }
                val angleRad = Math.toRadians(config.gradientAngle.toDouble())
                val x1 = (width / 2f - (width / 2f) * cos(angleRad)).toFloat()
                val y1 = (height / 2f - (height / 2f) * sin(angleRad)).toFloat()
                val x2 = (width / 2f + (width / 2f) * cos(angleRad)).toFloat()
                val y2 = (height / 2f + (height / 2f) * sin(angleRad)).toFloat()

                val shader = LinearGradient(x1, y1, x2, y2, colors, null, Shader.TileMode.CLAMP)
                paint.shader = shader
                canvas.drawRect(0f, 0f, width, height, paint)
            }
            BackgroundType.IMAGE -> {
                if (!config.imageUri.isNullOrBlank()) {
                    try {
                        val uri = Uri.parse(config.imageUri)
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val bgBmp = BitmapFactory.decodeStream(inputStream)
                        inputStream?.close()
                        if (bgBmp != null) {
                            val srcRect = Rect(0, 0, bgBmp.width, bgBmp.height)
                            val dstRect = RectF(0f, 0f, width, height)
                            canvas.drawBitmap(bgBmp, srcRect, dstRect, paint)
                        } else {
                            paint.color = Color.BLACK
                            canvas.drawRect(0f, 0f, width, height, paint)
                        }
                    } catch (e: Exception) {
                        paint.color = Color.BLACK
                        canvas.drawRect(0f, 0f, width, height, paint)
                    }
                } else {
                    paint.color = Color.BLACK
                    canvas.drawRect(0f, 0f, width, height, paint)
                }
            }
            BackgroundType.TRANSPARENT -> {
                // Keep transparent ARGB alpha 0
            }
        }
    }

    private fun drawLayer(
        context: Context,
        canvas: Canvas,
        layer: CanvasLayer,
        canvasW: Float,
        canvasH: Float,
        scaleMultiplier: Float
    ) {
        val centerX = layer.xPercent * canvasW
        val centerY = layer.yPercent * canvasH
        val layerW = layer.widthPercent * canvasW
        val layerH = layer.heightPercent * canvasH

        canvas.save()
        canvas.translate(centerX, centerY)
        canvas.rotate(layer.rotationDeg)
        canvas.translate(-layerW / 2f, -layerH / 2f)

        val alphaInt = (layer.opacity.coerceIn(0f, 1f) * 255).toInt()

        when (layer.type) {
            LayerType.TEXT -> drawTextLayer(context, canvas, layer, layerW, layerH, scaleMultiplier, alphaInt)
            LayerType.IMAGE -> drawImageLayer(context, canvas, layer, layerW, layerH, scaleMultiplier, alphaInt)
            LayerType.SHAPE -> drawShapeLayer(canvas, layer, layerW, layerH, scaleMultiplier, alphaInt)
            LayerType.STICKER -> drawStickerLayer(canvas, layer, layerW, layerH, scaleMultiplier, alphaInt)
        }

        canvas.restore()
    }

    private fun drawTextLayer(
        context: Context,
        canvas: Canvas,
        layer: CanvasLayer,
        layerW: Float,
        layerH: Float,
        scaleMultiplier: Float,
        alphaInt: Int
    ) {
        if (layer.text.isBlank()) return

        // Background highlight
        if (layer.backgroundColor != 0x00000000L) {
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = layer.backgroundColor.toInt()
                alpha = alphaInt
            }
            val radius = layer.backgroundCornerRadiusDp * scaleMultiplier
            val rect = RectF(0f, 0f, layerW, layerH)
            canvas.drawRoundRect(rect, radius, radius, bgPaint)
        }

        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = layer.textColor.toInt()
            alpha = alphaInt
            textSize = layer.fontSizeSp * 2.2f * scaleMultiplier

            val baseTypeface = FontHelper.getNativeTypeface(context, layer.fontFamily)
            val style = when {
                layer.isBold && layer.isItalic -> Typeface.BOLD_ITALIC
                layer.isBold -> Typeface.BOLD
                layer.isItalic -> Typeface.ITALIC
                else -> Typeface.NORMAL
            }
            typeface = Typeface.create(baseTypeface, style)

            if (layer.shadowColor != 0x00000000L && layer.shadowRadiusDp > 0f) {
                setShadowLayer(
                    layer.shadowRadiusDp * 2f * scaleMultiplier,
                    layer.shadowDxDp * 2f * scaleMultiplier,
                    layer.shadowDyDp * 2f * scaleMultiplier,
                    layer.shadowColor.toInt()
                )
            }
        }

        val alignment = when (layer.textAlign) {
            CanvasTextAlign.LEFT -> Layout.Alignment.ALIGN_NORMAL
            CanvasTextAlign.CENTER -> Layout.Alignment.ALIGN_CENTER
            CanvasTextAlign.RIGHT -> Layout.Alignment.ALIGN_OPPOSITE
        }

        val staticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(layer.text, 0, layer.text.length, textPaint, layerW.toInt().coerceAtLeast(10))
                .setAlignment(alignment)
                .setLineSpacing(0f, 1.15f)
                .setIncludePad(true)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(layer.text, textPaint, layerW.toInt().coerceAtLeast(10), alignment, 1.15f, 0f, true)
        }

        canvas.save()
        val textTopOffset = ((layerH - staticLayout.height) / 2f).coerceAtLeast(0f)
        canvas.translate(0f, textTopOffset)

        if (layer.strokeColor != 0x00000000L && layer.strokeWidthDp > 0f) {
            val strokePaint = TextPaint(textPaint).apply {
                style = Paint.Style.STROKE
                strokeWidth = layer.strokeWidthDp * 2f * scaleMultiplier
                color = layer.strokeColor.toInt()
                alpha = alphaInt
            }
            val strokeLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                StaticLayout.Builder.obtain(layer.text, 0, layer.text.length, strokePaint, layerW.toInt().coerceAtLeast(10))
                    .setAlignment(alignment)
                    .setLineSpacing(0f, 1.15f)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                StaticLayout(layer.text, strokePaint, layerW.toInt().coerceAtLeast(10), alignment, 1.15f, 0f, true)
            }
            strokeLayout.draw(canvas)
        }

        staticLayout.draw(canvas)
        canvas.restore()
    }

    private fun drawImageLayer(
        context: Context,
        canvas: Canvas,
        layer: CanvasLayer,
        layerW: Float,
        layerH: Float,
        scaleMultiplier: Float,
        alphaInt: Int
    ) {
        if (layer.imageUri.isNullOrBlank()) return

        try {
            val bitmap = if (layer.imageUri.startsWith("file://") || layer.imageUri.startsWith("/")) {
                val path = Uri.parse(layer.imageUri).path ?: layer.imageUri.removePrefix("file://")
                BitmapFactory.decodeFile(path)
            } else {
                val uri = Uri.parse(layer.imageUri)
                val inputStream = context.contentResolver.openInputStream(uri)
                val bmp = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                bmp
            }

            if (bitmap != null) {
                val cr = layer.imageCornerRadiusDp * (layerW / 100f) * scaleMultiplier

                // Draw Image Shadow if set
                if (layer.imageShadowColor != 0x00000000L && layer.imageShadowRadiusDp > 0f) {
                    val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = layer.imageShadowColor.toInt()
                        maskFilter = BlurMaskFilter(layer.imageShadowRadiusDp * 2f * scaleMultiplier, BlurMaskFilter.Blur.NORMAL)
                    }
                    val shadowRect = RectF(
                        layer.imageShadowDxDp * scaleMultiplier,
                        layer.imageShadowDyDp * scaleMultiplier,
                        layerW + layer.imageShadowDxDp * scaleMultiplier,
                        layerH + layer.imageShadowDyDp * scaleMultiplier
                    )
                    canvas.drawRoundRect(shadowRect, cr, cr, shadowPaint)
                }

                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    alpha = alphaInt
                    val filter = PhotoEnhancer.buildAndroidColorFilter(layer.adjustments)
                    if (filter != null) {
                        colorFilter = filter
                    } else if (layer.imageTintColor != 0x00000000L) {
                        colorFilter = PorterDuffColorFilter(layer.imageTintColor.toInt(), PorterDuff.Mode.SRC_ATOP)
                    }
                }

                canvas.save()
                if (layer.flipHorizontal || layer.flipVertical) {
                    val sx = if (layer.flipHorizontal) -1f else 1f
                    val sy = if (layer.flipVertical) -1f else 1f
                    canvas.scale(sx, sy, layerW / 2f, layerH / 2f)
                }

                if (cr > 0f) {
                    val path = Path()
                    path.addRoundRect(RectF(0f, 0f, layerW, layerH), cr, cr, Path.Direction.CW)
                    canvas.clipPath(path)
                }

                val srcRect = Rect(0, 0, bitmap.width, bitmap.height)
                val dstRect = RectF(0f, 0f, layerW, layerH)
                canvas.drawBitmap(bitmap, srcRect, dstRect, paint)
                canvas.restore()

                // Draw Highlight Border for Image
                if (layer.imageBorderColor != 0x00000000L && layer.imageBorderWidthDp > 0f) {
                    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = layer.imageBorderWidthDp * 2f * scaleMultiplier
                        color = layer.imageBorderColor.toInt()
                        alpha = alphaInt
                    }
                    val borderRect = RectF(0f, 0f, layerW, layerH)
                    canvas.drawRoundRect(borderRect, cr, cr, borderPaint)
                }
            }
        } catch (e: Exception) {
            val paint = Paint().apply {
                color = Color.DKGRAY
                alpha = alphaInt
            }
            canvas.drawRect(0f, 0f, layerW, layerH, paint)
        }
    }

    private fun drawShapeLayer(
        canvas: Canvas,
        layer: CanvasLayer,
        layerW: Float,
        layerH: Float,
        scaleMultiplier: Float,
        alphaInt: Int
    ) {
        val cr = layer.shapeCornerRadiusDp * scaleMultiplier
        val path = ShapeDefinitions.buildAndroidPath(
            layer.shapeType,
            layerW,
            layerH,
            cr
        )

        // Draw Shape Shadow
        if (layer.shapeShadowColor != 0x00000000L && layer.shapeShadowRadiusDp > 0f) {
            val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = layer.shapeShadowColor.toInt()
                maskFilter = BlurMaskFilter(layer.shapeShadowRadiusDp * 2f * scaleMultiplier, BlurMaskFilter.Blur.NORMAL)
            }
            canvas.save()
            canvas.translate(layer.shapeShadowDxDp * scaleMultiplier, layer.shapeShadowDyDp * scaleMultiplier)
            canvas.drawPath(path, shadowPaint)
            canvas.restore()
        }

        // Fill
        if (layer.fillColor != 0x00000000L && layer.shapeType != ShapeType.LINE) {
            val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = layer.fillColor.toInt()
                alpha = alphaInt
            }
            canvas.drawPath(path, fillPaint)
        }

        // Border / Stroke / Highlight
        if (layer.shapeBorderColor != 0x00000000L && (layer.shapeBorderWidthDp > 0f || layer.shapeType == ShapeType.LINE)) {
            val strokeWidth = if (layer.shapeType == ShapeType.LINE) {
                (layer.shapeBorderWidthDp.coerceAtLeast(3f)) * scaleMultiplier
            } else {
                layer.shapeBorderWidthDp * scaleMultiplier
            }
            val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                this.strokeWidth = strokeWidth
                color = layer.shapeBorderColor.toInt()
                alpha = alphaInt
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
            }
            canvas.drawPath(path, strokePaint)
        }
    }

    private fun drawStickerLayer(
        canvas: Canvas,
        layer: CanvasLayer,
        layerW: Float,
        layerH: Float,
        scaleMultiplier: Float,
        alphaInt: Int
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = minOf(layerW, layerH) * 0.75f
            textAlign = Paint.Align.CENTER
            color = layer.stickerColor.toInt()
            alpha = alphaInt

            if (layer.stickerShadowColor != 0x00000000L && layer.stickerShadowRadiusDp > 0f) {
                setShadowLayer(
                    layer.stickerShadowRadiusDp * 2f * scaleMultiplier,
                    layer.stickerShadowDxDp * 2f * scaleMultiplier,
                    layer.stickerShadowDyDp * 2f * scaleMultiplier,
                    layer.stickerShadowColor.toInt()
                )
            }
        }

        val fontMetrics = paint.fontMetrics
        val baseline = layerH / 2f - (fontMetrics.ascent + fontMetrics.descent) / 2f
        canvas.drawText(layer.stickerSymbol, layerW / 2f, baseline, paint)
    }

    private fun drawStroke(
        canvas: Canvas,
        stroke: DrawStroke,
        canvasW: Float,
        canvasH: Float,
        scaleMultiplier: Float
    ) {
        if (stroke.points.size < 2) return

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = stroke.color.toInt()
            strokeWidth = stroke.strokeWidthPercent * canvasW * scaleMultiplier
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            alpha = (stroke.opacity.coerceIn(0f, 1f) * 255).toInt()

            when (stroke.toolType) {
                DrawToolType.HIGHLIGHTER -> {
                    xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
                    alpha = (0.4f * 255).toInt()
                }
                DrawToolType.ERASER -> {
                    xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                }
                else -> {}
            }
        }

        val path = Path()
        val first = stroke.points.first()
        path.moveTo(first.x * canvasW, first.y * canvasH)

        for (i in 1 until stroke.points.size) {
            val pt = stroke.points[i]
            val prev = stroke.points[i - 1]
            val cx = (prev.x + pt.x) / 2f * canvasW
            val cy = (prev.y + pt.y) / 2f * canvasH
            path.quadTo(prev.x * canvasW, prev.y * canvasH, cx, cy)
        }

        val last = stroke.points.last()
        path.lineTo(last.x * canvasW, last.y * canvasH)

        canvas.drawPath(path, paint)
    }

    suspend fun saveBitmapToGallery(
        context: Context,
        bitmap: Bitmap,
        title: String,
        format: ExportFormat,
        qualityPercent: Int
    ): Uri? = withContext(Dispatchers.IO) {
        val fileName = "PixCraft_${title.replace("[^a-zA-Z0-9_]".toRegex(), "_")}_${System.currentTimeMillis()}.${if (format == ExportFormat.PNG) "png" else "jpg"}"
        val mimeType = if (format == ExportFormat.PNG) "image/png" else "image/jpeg"
        val compressFormat = if (format == ExportFormat.PNG) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/PixCraftPro")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            if (uri != null) {
                resolver.openOutputStream(uri)?.use { stream ->
                    bitmap.compress(compressFormat, qualityPercent, stream)
                }
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
                return@withContext uri
            }
        } else {
            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val appDir = File(picturesDir, "PixCraftPro")
            if (!appDir.exists()) appDir.mkdirs()
            val file = File(appDir, fileName)
            FileOutputStream(file).use { out ->
                bitmap.compress(compressFormat, qualityPercent, out)
            }
            return@withContext Uri.fromFile(file)
        }
        null
    }

    suspend fun exportAndShare(
        context: Context,
        bitmap: Bitmap,
        title: String,
        format: ExportFormat
    ) = withContext(Dispatchers.IO) {
        val cachePath = File(context.cacheDir, "images")
        if (!cachePath.exists()) cachePath.mkdirs()

        val extension = if (format == ExportFormat.PNG) "png" else "jpg"
        val compressFormat = if (format == ExportFormat.PNG) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
        val file = File(cachePath, "PixCraft_${System.currentTimeMillis()}.$extension")

        FileOutputStream(file).use { out ->
            bitmap.compress(compressFormat, 100, out)
        }

        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = if (format == ExportFormat.PNG) "image/png" else "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        withContext(Dispatchers.Main) {
            val chooser = Intent.createChooser(shareIntent, "Share PixCraft Design").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        }
    }
}
