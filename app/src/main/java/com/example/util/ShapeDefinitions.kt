package com.example.util

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import com.example.model.ShapeType
import kotlin.math.cos
import kotlin.math.sin

object ShapeDefinitions {

    fun buildComposePath(shapeType: ShapeType, size: Size, cornerRadiusPx: Float = 0f): Path {
        val path = Path()
        val w = size.width
        val h = size.height

        when (shapeType) {
            ShapeType.RECTANGLE -> {
                path.addRect(Rect(0f, 0f, w, h))
            }
            ShapeType.ROUNDED_RECTANGLE -> {
                val cr = if (cornerRadiusPx > 0f) cornerRadiusPx else (minOf(w, h) * 0.15f)
                path.addRoundRect(
                    RoundRect(
                        rect = Rect(0f, 0f, w, h),
                        cornerRadius = CornerRadius(cr, cr)
                    )
                )
            }
            ShapeType.CIRCLE -> {
                path.addOval(Rect(0f, 0f, w, h))
            }
            ShapeType.TRIANGLE -> {
                path.moveTo(w / 2f, 0f)
                path.lineTo(w, h)
                path.lineTo(0f, h)
                path.close()
            }
            ShapeType.LINE -> {
                path.moveTo(0f, h / 2f)
                path.lineTo(w, h / 2f)
            }
            ShapeType.ARROW -> {
                val headWidth = w * 0.35f
                val stemHeight = h * 0.4f
                val stemTop = (h - stemHeight) / 2f
                val stemBottom = stemTop + stemHeight
                val stemRight = w - headWidth

                path.moveTo(0f, stemTop)
                path.lineTo(stemRight, stemTop)
                path.lineTo(stemRight, 0f)
                path.lineTo(w, h / 2f)
                path.lineTo(stemRight, h)
                path.lineTo(stemRight, stemBottom)
                path.lineTo(0f, stemBottom)
                path.close()
            }
            ShapeType.STAR -> {
                val cx = w / 2f
                val cy = h / 2f
                val outerR = minOf(w, h) / 2f
                val innerR = outerR * 0.42f
                val points = 5
                val angleStep = Math.PI / points
                var angle = -Math.PI / 2.0

                path.moveTo(
                    (cx + outerR * cos(angle)).toFloat(),
                    (cy + outerR * sin(angle)).toFloat()
                )
                for (i in 0 until points * 2) {
                    val r = if (i % 2 == 0) outerR else innerR
                    val x = (cx + r * cos(angle)).toFloat()
                    val y = (cy + r * sin(angle)).toFloat()
                    path.lineTo(x, y)
                    angle += angleStep
                }
                path.close()
            }
            ShapeType.HEART -> {
                val cx = w / 2f
                path.moveTo(cx, h * 0.85f)
                path.cubicTo(
                    w * 0.05f, h * 0.55f,
                    w * 0.05f, h * 0.15f,
                    cx, h * 0.35f
                )
                path.cubicTo(
                    w * 0.95f, h * 0.15f,
                    w * 0.95f, h * 0.55f,
                    cx, h * 0.85f
                )
                path.close()
            }
            ShapeType.HEXAGON -> {
                val cx = w / 2f
                val cy = h / 2f
                val rx = w / 2f
                val ry = h / 2f
                for (i in 0 until 6) {
                    val angle = Math.PI / 3.0 * i - Math.PI / 6.0
                    val x = (cx + rx * cos(angle)).toFloat()
                    val y = (cy + ry * sin(angle)).toFloat()
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
            }
        }
        return path
    }

    fun buildAndroidPath(shapeType: ShapeType, width: Float, height: Float, cornerRadiusPx: Float = 0f): android.graphics.Path {
        val path = android.graphics.Path()
        when (shapeType) {
            ShapeType.RECTANGLE -> {
                path.addRect(0f, 0f, width, height, android.graphics.Path.Direction.CW)
            }
            ShapeType.ROUNDED_RECTANGLE -> {
                val cr = if (cornerRadiusPx > 0f) cornerRadiusPx else (minOf(width, height) * 0.15f)
                path.addRoundRect(
                    0f, 0f, width, height,
                    floatArrayOf(cr, cr, cr, cr, cr, cr, cr, cr),
                    android.graphics.Path.Direction.CW
                )
            }
            ShapeType.CIRCLE -> {
                path.addOval(0f, 0f, width, height, android.graphics.Path.Direction.CW)
            }
            ShapeType.TRIANGLE -> {
                path.moveTo(width / 2f, 0f)
                path.lineTo(width, height)
                path.lineTo(0f, height)
                path.close()
            }
            ShapeType.LINE -> {
                path.moveTo(0f, height / 2f)
                path.lineTo(width, height / 2f)
            }
            ShapeType.ARROW -> {
                val headWidth = width * 0.35f
                val stemHeight = height * 0.4f
                val stemTop = (height - stemHeight) / 2f
                val stemBottom = stemTop + stemHeight
                val stemRight = width - headWidth

                path.moveTo(0f, stemTop)
                path.lineTo(stemRight, stemTop)
                path.lineTo(stemRight, 0f)
                path.lineTo(width, height / 2f)
                path.lineTo(stemRight, height)
                path.lineTo(stemRight, stemBottom)
                path.lineTo(0f, stemBottom)
                path.close()
            }
            ShapeType.STAR -> {
                val cx = width / 2f
                val cy = height / 2f
                val outerR = minOf(width, height) / 2f
                val innerR = outerR * 0.42f
                val points = 5
                val angleStep = Math.PI / points
                var angle = -Math.PI / 2.0

                path.moveTo(
                    (cx + outerR * cos(angle)).toFloat(),
                    (cy + outerR * sin(angle)).toFloat()
                )
                for (i in 0 until points * 2) {
                    val r = if (i % 2 == 0) outerR else innerR
                    val x = (cx + r * cos(angle)).toFloat()
                    val y = (cy + r * sin(angle)).toFloat()
                    path.lineTo(x, y)
                    angle += angleStep
                }
                path.close()
            }
            ShapeType.HEART -> {
                val cx = width / 2f
                path.moveTo(cx, height * 0.85f)
                path.cubicTo(
                    width * 0.05f, height * 0.55f,
                    width * 0.05f, height * 0.15f,
                    cx, height * 0.35f
                )
                path.cubicTo(
                    width * 0.95f, height * 0.15f,
                    width * 0.95f, height * 0.55f,
                    cx, height * 0.85f
                )
                path.close()
            }
            ShapeType.HEXAGON -> {
                val cx = width / 2f
                val cy = height / 2f
                val rx = width / 2f
                val ry = height / 2f
                for (i in 0 until 6) {
                    val angle = Math.PI / 3.0 * i - Math.PI / 6.0
                    val x = (cx + rx * cos(angle)).toFloat()
                    val y = (cy + ry * sin(angle)).toFloat()
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
            }
        }
        return path
    }
}
