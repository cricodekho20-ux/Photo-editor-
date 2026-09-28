package com.example.util

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import androidx.compose.ui.graphics.ColorFilter
import com.example.model.ImageAdjustments
import com.example.model.PhotoFilterPreset

object PhotoEnhancer {

    fun buildAndroidColorMatrix(adjustments: ImageAdjustments): ColorMatrix {
        val matrix = ColorMatrix()

        // 1. Base Filter Presets
        when (adjustments.filterPreset) {
            PhotoFilterPreset.MAGIC_AUTO -> {
                // Auto HD Enhance: crisp contrast & boost saturation
                val autoMat = ColorMatrix(
                    floatArrayOf(
                        1.2f, 0f, 0f, 0f, 10f,
                        0f, 1.2f, 0f, 0f, 10f,
                        0f, 0f, 1.2f, 0f, 10f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                matrix.postConcat(autoMat)
            }
            PhotoFilterPreset.VIVID_POP -> {
                val satMat = ColorMatrix()
                satMat.setSaturation(1.6f)
                val conMat = ColorMatrix(
                    floatArrayOf(
                        1.15f, 0f, 0f, 0f, 5f,
                        0f, 1.15f, 0f, 0f, 5f,
                        0f, 0f, 1.15f, 0f, 5f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                matrix.postConcat(satMat)
                matrix.postConcat(conMat)
            }
            PhotoFilterPreset.GOLDEN_HOUR -> {
                // Warm golden amber tint
                val warmMat = ColorMatrix(
                    floatArrayOf(
                        1.25f, 0f, 0f, 0f, 25f,
                        0f, 1.1f, 0f, 0f, 15f,
                        0f, 0f, 0.9f, 0f, -10f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                matrix.postConcat(warmMat)
            }
            PhotoFilterPreset.CYBER_NEON -> {
                // Violet & cyan punch
                val neonMat = ColorMatrix(
                    floatArrayOf(
                        1.2f, 0.1f, 0.2f, 0f, 15f,
                        0.1f, 1.1f, 0.3f, 0f, 10f,
                        0.2f, 0.1f, 1.35f, 0f, 25f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                matrix.postConcat(neonMat)
            }
            PhotoFilterPreset.NOIR_BW -> {
                val bwMat = ColorMatrix()
                bwMat.setSaturation(0f)
                val contrastMat = ColorMatrix(
                    floatArrayOf(
                        1.3f, 0f, 0f, 0f, -20f,
                        0f, 1.3f, 0f, 0f, -20f,
                        0f, 0f, 1.3f, 0f, -20f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                matrix.postConcat(bwMat)
                matrix.postConcat(contrastMat)
            }
            PhotoFilterPreset.VINTAGE_SEPIA -> {
                val sepiaMat = ColorMatrix(
                    floatArrayOf(
                        0.393f, 0.769f, 0.189f, 0f, 20f,
                        0.349f, 0.686f, 0.168f, 0f, 10f,
                        0.272f, 0.534f, 0.131f, 0f, 0f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                matrix.postConcat(sepiaMat)
            }
            PhotoFilterPreset.HDR_DRAMA -> {
                val hdrMat = ColorMatrix(
                    floatArrayOf(
                        1.3f, -0.1f, -0.1f, 0f, 15f,
                        -0.1f, 1.3f, -0.1f, 0f, 15f,
                        -0.1f, -0.1f, 1.3f, 0f, 15f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                matrix.postConcat(hdrMat)
            }
            PhotoFilterPreset.FILM_MATTE -> {
                val matteMat = ColorMatrix(
                    floatArrayOf(
                        0.95f, 0f, 0f, 0f, 25f,
                        0f, 0.95f, 0f, 0f, 25f,
                        0f, 0f, 0.95f, 0f, 25f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                matrix.postConcat(matteMat)
            }
            PhotoFilterPreset.NONE -> {}
        }

        // 2. Brightness
        if (adjustments.brightness != 0f) {
            val b = adjustments.brightness * 100f
            val brightMat = ColorMatrix(
                floatArrayOf(
                    1f, 0f, 0f, 0f, b,
                    0f, 1f, 0f, 0f, b,
                    0f, 0f, 1f, 0f, b,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            matrix.postConcat(brightMat)
        }

        // 3. Contrast
        if (adjustments.contrast != 1.0f) {
            val scale = adjustments.contrast
            val translate = (-0.5f * scale + 0.5f) * 255f
            val conMat = ColorMatrix(
                floatArrayOf(
                    scale, 0f, 0f, 0f, translate,
                    0f, scale, 0f, 0f, translate,
                    0f, 0f, scale, 0f, translate,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            matrix.postConcat(conMat)
        }

        // 4. Saturation
        if (adjustments.saturation != 1.0f) {
            val satMat = ColorMatrix()
            satMat.setSaturation(adjustments.saturation)
            matrix.postConcat(satMat)
        }

        // 5. Warmth / Temperature
        if (adjustments.warmth != 0f) {
            val w = adjustments.warmth * 40f
            val warmMat = ColorMatrix(
                floatArrayOf(
                    1f, 0f, 0f, 0f, w,
                    0f, 1f, 0f, 0f, w * 0.5f,
                    0f, 0f, 1f, 0f, -w,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            matrix.postConcat(warmMat)
        }

        return matrix
    }

    fun buildComposeColorFilter(adjustments: ImageAdjustments): ColorFilter? {
        if (adjustments.filterPreset == PhotoFilterPreset.NONE &&
            adjustments.brightness == 0f &&
            adjustments.contrast == 1f &&
            adjustments.saturation == 1f &&
            adjustments.warmth == 0f
        ) {
            return null
        }
        val androidMat = buildAndroidColorMatrix(adjustments)
        return ColorFilter.colorMatrix(androidx.compose.ui.graphics.ColorMatrix(androidMat.array))
    }

    fun buildAndroidColorFilter(adjustments: ImageAdjustments): ColorMatrixColorFilter? {
        if (adjustments.filterPreset == PhotoFilterPreset.NONE &&
            adjustments.brightness == 0f &&
            adjustments.contrast == 1f &&
            adjustments.saturation == 1f &&
            adjustments.warmth == 0f
        ) {
            return null
        }
        val matrix = buildAndroidColorMatrix(adjustments)
        return ColorMatrixColorFilter(matrix)
    }
}
