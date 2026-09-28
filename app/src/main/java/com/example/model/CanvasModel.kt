package com.example.model

import com.squareup.moshi.JsonClass

enum class BackgroundType {
    SOLID,
    GRADIENT,
    IMAGE,
    TRANSPARENT
}

@JsonClass(generateAdapter = true)
data class BackgroundConfig(
    val type: BackgroundType = BackgroundType.SOLID,
    val solidColor: Long = 0xFF0A0B10,
    val gradientColors: List<Long> = listOf(0xFF7C3AED, 0xFFFFB300),
    val gradientAngle: Float = 45f,
    val imageUri: String? = null
)

enum class LayerType {
    TEXT,
    IMAGE,
    SHAPE,
    STICKER
}

enum class ShapeType {
    RECTANGLE,
    ROUNDED_RECTANGLE,
    CIRCLE,
    TRIANGLE,
    LINE,
    ARROW,
    STAR,
    HEART,
    HEXAGON
}

enum class TextFontFamily {
    DEFAULT,
    HINDI_ROZHA,      // Rozha One (Trending Heavy Hindi Poster)
    HINDI_KALAM,      // Kalam (Trending Hindi Calligraphy)
    HINDI_YATRA,      // Yatra One (Trending Festival Hindi)
    ENGLISH_BEBAS,    // Bebas Neue (Trending Impact / Bold Poster)
    ENGLISH_PACIFICO, // Pacifico (Trending Script / Handwritten)
    ENGLISH_PLAYFAIR, // Playfair Display (Trending Luxury Serif)
    ENGLISH_CINZEL,   // Cinzel (Trending Cinematic Serif)
    SERIF,
    MONOSPACE,
    CURSIVE,
    BOLD_DISPLAY,
    MODERN_CLEAN
}

enum class CanvasTextAlign {
    LEFT,
    CENTER,
    RIGHT
}

enum class PhotoFilterPreset {
    NONE,
    MAGIC_AUTO,
    VIVID_POP,
    GOLDEN_HOUR,
    CYBER_NEON,
    NOIR_BW,
    VINTAGE_SEPIA,
    HDR_DRAMA,
    FILM_MATTE
}

@JsonClass(generateAdapter = true)
data class ImageAdjustments(
    val filterPreset: PhotoFilterPreset = PhotoFilterPreset.NONE,
    val brightness: Float = 0f,      // -1.0 .. +1.0
    val contrast: Float = 1.0f,      // 0.5 .. 2.0
    val saturation: Float = 1.0f,    // 0.0 .. 2.5
    val warmth: Float = 0f,          // -1.0 .. +1.0
    val isAutoEnhanced: Boolean = false
)

@JsonClass(generateAdapter = true)
data class CanvasLayer(
    val id: String,
    val name: String,
    val type: LayerType,
    val isVisible: Boolean = true,
    val isLocked: Boolean = false,

    // Position & Size normalized relative to Canvas dimensions (0.0f .. 1.0f)
    val xPercent: Float = 0.5f,
    val yPercent: Float = 0.5f,
    val widthPercent: Float = 0.4f,
    val heightPercent: Float = 0.2f,
    val rotationDeg: Float = 0f,
    val opacity: Float = 1.0f,

    // Text Layer Properties
    val text: String = "Your Text",
    val fontFamily: TextFontFamily = TextFontFamily.DEFAULT,
    val fontSizeSp: Float = 28f,
    val textColor: Long = 0xFFFFFFFF,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val textAlign: CanvasTextAlign = CanvasTextAlign.CENTER,
    val strokeColor: Long = 0x00000000,
    val strokeWidthDp: Float = 0f,
    val shadowColor: Long = 0x00000000,
    val shadowRadiusDp: Float = 0f,
    val shadowDxDp: Float = 0f,
    val shadowDyDp: Float = 0f,
    val backgroundColor: Long = 0x00000000,
    val backgroundCornerRadiusDp: Float = 0f,
    val backgroundPaddingDp: Float = 0f,

    // Image Layer Properties
    val imageUri: String? = null,
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false,
    val imageCornerRadiusDp: Float = 0f,
    val imageTintColor: Long = 0x00000000,
    val imageBorderColor: Long = 0x00000000,
    val imageBorderWidthDp: Float = 0f,
    val imageShadowColor: Long = 0x00000000,
    val imageShadowRadiusDp: Float = 0f,
    val imageShadowDxDp: Float = 0f,
    val imageShadowDyDp: Float = 0f,
    val adjustments: ImageAdjustments = ImageAdjustments(),

    // Shape Layer Properties
    val shapeType: ShapeType = ShapeType.RECTANGLE,
    val fillColor: Long = 0xFF7C3AED,
    val shapeBorderColor: Long = 0xFFFFB300,
    val shapeBorderWidthDp: Float = 0f,
    val shapeCornerRadiusDp: Float = 12f,
    val shapeShadowColor: Long = 0x00000000,
    val shapeShadowRadiusDp: Float = 0f,
    val shapeShadowDxDp: Float = 0f,
    val shapeShadowDyDp: Float = 0f,

    // Sticker Layer Properties
    val stickerCategory: String = "Emoji",
    val stickerSymbol: String = "✨",
    val stickerColor: Long = 0xFFFFFFFF,
    val stickerShadowColor: Long = 0x00000000,
    val stickerShadowRadiusDp: Float = 0f,
    val stickerShadowDxDp: Float = 0f,
    val stickerShadowDyDp: Float = 0f
)

enum class DrawToolType {
    PEN,
    BRUSH,
    MARKER,
    HIGHLIGHTER,
    ERASER
}

@JsonClass(generateAdapter = true)
data class PointF2(
    val x: Float,
    val y: Float
)

@JsonClass(generateAdapter = true)
data class DrawStroke(
    val id: String,
    val points: List<PointF2>,
    val toolType: DrawToolType = DrawToolType.PEN,
    val color: Long = 0xFFFFB300,
    val strokeWidthPercent: Float = 0.015f,
    val opacity: Float = 1.0f
)

data class AspectRatioPreset(
    val label: String,
    val ratioDescription: String,
    val width: Int,
    val height: Int,
    val iconName: String
) {
    val aspectRatio: Float get() = width.toFloat() / height.toFloat()
}

val STANDARD_RATIO_PRESETS = listOf(
    AspectRatioPreset("9:16", "Story / Reels / Status", 1080, 1920, "smartphone"),
    AspectRatioPreset("1:1", "Square Post", 1080, 1080, "crop_square"),
    AspectRatioPreset("4:3", "Classic Photo", 1440, 1080, "crop_portrait"),
    AspectRatioPreset("16:9", "Banner / YouTube", 1920, 1080, "crop_landscape")
)
