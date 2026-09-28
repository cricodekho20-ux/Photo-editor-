package com.example.data

import com.example.model.*
import java.util.UUID

object DefaultTemplates {
    fun getTemplates(): List<ProjectTemplate> {
        return listOf(
            ProjectTemplate(
                id = "template_mega_sale",
                title = "Mega Sale 50% OFF",
                category = "Business / Promo",
                width = 1080,
                height = 1080,
                aspectRatioLabel = "1:1",
                backgroundConfig = BackgroundConfig(
                    type = BackgroundType.GRADIENT,
                    gradientColors = listOf(0xFF0F172A, 0xFF7C3AED),
                    gradientAngle = 135f
                ),
                previewGradientColors = listOf(0xFF0F172A, 0xFF7C3AED),
                layers = listOf(
                    CanvasLayer(
                        id = UUID.randomUUID().toString(),
                        name = "Card Backdrop",
                        type = LayerType.SHAPE,
                        shapeType = ShapeType.ROUNDED_RECTANGLE,
                        xPercent = 0.5f,
                        yPercent = 0.5f,
                        widthPercent = 0.88f,
                        heightPercent = 0.88f,
                        fillColor = 0x1AFFFFFF,
                        shapeBorderColor = 0xFFFFB300,
                        shapeBorderWidthDp = 2f,
                        shapeCornerRadiusDp = 24f
                    ),
                    CanvasLayer(
                        id = UUID.randomUUID().toString(),
                        name = "Special Badge",
                        type = LayerType.SHAPE,
                        shapeType = ShapeType.ROUNDED_RECTANGLE,
                        xPercent = 0.5f,
                        yPercent = 0.26f,
                        widthPercent = 0.48f,
                        heightPercent = 0.09f,
                        fillColor = 0xFFF43F5E,
                        shapeCornerRadiusDp = 20f
                    ),
                    CanvasLayer(
                        id = UUID.randomUUID().toString(),
                        name = "LIMITED OFFER",
                        type = LayerType.TEXT,
                        text = "LIMITED TIME OFFER",
                        fontFamily = TextFontFamily.ENGLISH_BEBAS,
                        fontSizeSp = 22f,
                        textColor = 0xFFFFFFFF,
                        isBold = true,
                        xPercent = 0.5f,
                        yPercent = 0.26f,
                        widthPercent = 0.46f,
                        heightPercent = 0.08f
                    ),
                    CanvasLayer(
                        id = UUID.randomUUID().toString(),
                        name = "MEGA SALE",
                        type = LayerType.TEXT,
                        text = "BIG FLASH\nSALE 50%",
                        fontFamily = TextFontFamily.ENGLISH_BEBAS,
                        fontSizeSp = 56f,
                        textColor = 0xFFFFB300,
                        isBold = true,
                        shadowColor = 0x80000000,
                        shadowRadiusDp = 8f,
                        shadowDyDp = 4f,
                        xPercent = 0.5f,
                        yPercent = 0.50f,
                        widthPercent = 0.82f,
                        heightPercent = 0.28f
                    ),
                    CanvasLayer(
                        id = UUID.randomUUID().toString(),
                        name = "Shop Now Subtitle",
                        type = LayerType.TEXT,
                        text = "Use Code: CRAFT50 • Valid this Weekend",
                        fontFamily = TextFontFamily.MODERN_CLEAN,
                        fontSizeSp = 16f,
                        textColor = 0xFFE0E7FF,
                        xPercent = 0.5f,
                        yPercent = 0.74f,
                        widthPercent = 0.78f,
                        heightPercent = 0.08f
                    ),
                    CanvasLayer(
                        id = UUID.randomUUID().toString(),
                        name = "Sparkle Icon",
                        type = LayerType.STICKER,
                        stickerCategory = "Decorative",
                        stickerSymbol = "✨",
                        xPercent = 0.82f,
                        yPercent = 0.24f,
                        widthPercent = 0.16f,
                        heightPercent = 0.16f
                    )
                )
            ),
            ProjectTemplate(
                id = "template_festival_wishes",
                title = "Diwali & Festival Wishes",
                category = "Festival / Greeting",
                width = 1080,
                height = 1920,
                aspectRatioLabel = "9:16",
                backgroundConfig = BackgroundConfig(
                    type = BackgroundType.GRADIENT,
                    gradientColors = listOf(0xFF3B0764, 0xFF831843),
                    gradientAngle = 90f
                ),
                previewGradientColors = listOf(0xFF3B0764, 0xFF831843),
                layers = listOf(
                    CanvasLayer(
                        id = UUID.randomUUID().toString(),
                        name = "Festival Diya",
                        type = LayerType.STICKER,
                        stickerCategory = "Festival",
                        stickerSymbol = "🪔",
                        xPercent = 0.5f,
                        yPercent = 0.30f,
                        widthPercent = 0.32f,
                        heightPercent = 0.32f
                    ),
                    CanvasLayer(
                        id = UUID.randomUUID().toString(),
                        name = "Hindi Greetings",
                        type = LayerType.TEXT,
                        text = "शुभ दीपावली\nकी हार्दिक शुभकामनाएं",
                        fontFamily = TextFontFamily.HINDI_ROZHA,
                        fontSizeSp = 42f,
                        textColor = 0xFFFFB300,
                        isBold = true,
                        shadowColor = 0x80000000,
                        shadowRadiusDp = 6f,
                        xPercent = 0.5f,
                        yPercent = 0.52f,
                        widthPercent = 0.88f,
                        heightPercent = 0.22f
                    ),
                    CanvasLayer(
                        id = UUID.randomUUID().toString(),
                        name = "English Greeting",
                        type = LayerType.TEXT,
                        text = "May this festival of lights illuminate your life with happiness and good health.",
                        fontFamily = TextFontFamily.ENGLISH_PACIFICO,
                        fontSizeSp = 20f,
                        textColor = 0xFFFDF4FF,
                        xPercent = 0.5f,
                        yPercent = 0.70f,
                        widthPercent = 0.85f,
                        heightPercent = 0.15f
                    ),
                    CanvasLayer(
                        id = UUID.randomUUID().toString(),
                        name = "Fireworks Left",
                        type = LayerType.STICKER,
                        stickerCategory = "Festival",
                        stickerSymbol = "🎆",
                        xPercent = 0.2f,
                        yPercent = 0.18f,
                        widthPercent = 0.18f,
                        heightPercent = 0.18f
                    ),
                    CanvasLayer(
                        id = UUID.randomUUID().toString(),
                        name = "Fireworks Right",
                        type = LayerType.STICKER,
                        stickerCategory = "Festival",
                        stickerSymbol = "🎆",
                        xPercent = 0.8f,
                        yPercent = 0.18f,
                        widthPercent = 0.18f,
                        heightPercent = 0.18f
                    )
                )
            ),
            ProjectTemplate(
                id = "template_youtube_thumbnail",
                title = "Viral YouTube Thumbnail",
                category = "Social / YouTube",
                width = 1920,
                height = 1080,
                aspectRatioLabel = "16:9",
                backgroundConfig = BackgroundConfig(
                    type = BackgroundType.GRADIENT,
                    gradientColors = listOf(0xFF0F172A, 0xFF1E293B),
                    gradientAngle = 45f
                ),
                previewGradientColors = listOf(0xFF0F172A, 0xFF1E293B),
                layers = listOf(
                    CanvasLayer(
                        id = UUID.randomUUID().toString(),
                        name = "Highlight Strip",
                        type = LayerType.SHAPE,
                        shapeType = ShapeType.ROUNDED_RECTANGLE,
                        xPercent = 0.52f,
                        yPercent = 0.42f,
                        widthPercent = 0.76f,
                        heightPercent = 0.32f,
                        fillColor = 0xFFEF4444,
                        shapeCornerRadiusDp = 16f
                    ),
                    CanvasLayer(
                        id = UUID.randomUUID().toString(),
                        name = "Catchy Title",
                        type = LayerType.TEXT,
                        text = "I TESTED THIS FOR 30 DAYS!",
                        fontFamily = TextFontFamily.ENGLISH_BEBAS,
                        fontSizeSp = 52f,
                        textColor = 0xFFFFFFFF,
                        isBold = true,
                        xPercent = 0.52f,
                        yPercent = 0.42f,
                        widthPercent = 0.74f,
                        heightPercent = 0.26f
                    ),
                    CanvasLayer(
                        id = UUID.randomUUID().toString(),
                        name = "Badge",
                        type = LayerType.SHAPE,
                        shapeType = ShapeType.ROUNDED_RECTANGLE,
                        xPercent = 0.32f,
                        yPercent = 0.74f,
                        widthPercent = 0.36f,
                        heightPercent = 0.16f,
                        fillColor = 0xFFFFB300,
                        shapeCornerRadiusDp = 12f
                    ),
                    CanvasLayer(
                        id = UUID.randomUUID().toString(),
                        name = "Badge Text",
                        type = LayerType.TEXT,
                        text = "SHOCKING RESULTS",
                        fontFamily = TextFontFamily.ENGLISH_BEBAS,
                        fontSizeSp = 24f,
                        textColor = 0xFF000000,
                        isBold = true,
                        xPercent = 0.32f,
                        yPercent = 0.74f,
                        widthPercent = 0.34f,
                        heightPercent = 0.12f
                    ),
                    CanvasLayer(
                        id = UUID.randomUUID().toString(),
                        name = "Fire Emoji",
                        type = LayerType.STICKER,
                        stickerCategory = "Emoji",
                        stickerSymbol = "🔥",
                        xPercent = 0.85f,
                        yPercent = 0.74f,
                        widthPercent = 0.16f,
                        heightPercent = 0.16f
                    )
                )
            ),
            ProjectTemplate(
                id = "template_quote_poster",
                title = "Inspiring Quote Poster",
                category = "Typography",
                width = 1080,
                height = 1080,
                aspectRatioLabel = "1:1",
                backgroundConfig = BackgroundConfig(
                    type = BackgroundType.GRADIENT,
                    gradientColors = listOf(0xFF0F172A, 0xFF1E1B4B),
                    gradientAngle = 180f
                ),
                previewGradientColors = listOf(0xFF0F172A, 0xFF1E1B4B),
                layers = listOf(
                    CanvasLayer(
                        id = UUID.randomUUID().toString(),
                        name = "Inner Frame",
                        type = LayerType.SHAPE,
                        shapeType = ShapeType.RECTANGLE,
                        xPercent = 0.5f,
                        yPercent = 0.5f,
                        widthPercent = 0.88f,
                        heightPercent = 0.88f,
                        fillColor = 0x00000000,
                        shapeBorderColor = 0xFFFFB300,
                        shapeBorderWidthDp = 3f
                    ),
                    CanvasLayer(
                        id = UUID.randomUUID().toString(),
                        name = "Quote Text",
                        type = LayerType.TEXT,
                        text = "“Great things never come from comfort zones.”",
                        fontFamily = TextFontFamily.ENGLISH_PLAYFAIR,
                        fontSizeSp = 34f,
                        textColor = 0xFFF8FAFC,
                        isItalic = true,
                        xPercent = 0.5f,
                        yPercent = 0.46f,
                        widthPercent = 0.80f,
                        heightPercent = 0.35f
                    ),
                    CanvasLayer(
                        id = UUID.randomUUID().toString(),
                        name = "Author",
                        type = LayerType.TEXT,
                        text = "— CRAFT YOUR VISION",
                        fontFamily = TextFontFamily.ENGLISH_CINZEL,
                        fontSizeSp = 18f,
                        textColor = 0xFFFFB300,
                        isBold = true,
                        xPercent = 0.5f,
                        yPercent = 0.75f,
                        widthPercent = 0.72f,
                        heightPercent = 0.08f
                    )
                )
            )
        )
    }
}
