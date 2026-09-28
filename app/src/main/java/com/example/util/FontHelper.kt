package com.example.util

import android.content.Context
import android.graphics.Typeface
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.model.TextFontFamily

object FontHelper {

    fun getComposeFontFamily(font: TextFontFamily): FontFamily {
        return when (font) {
            TextFontFamily.HINDI_ROZHA -> FontFamily(Font(R.font.rozha_one, FontWeight.Normal))
            TextFontFamily.HINDI_KALAM -> FontFamily(Font(R.font.kalam, FontWeight.Normal))
            TextFontFamily.HINDI_YATRA -> FontFamily(Font(R.font.yatra_one, FontWeight.Normal))
            TextFontFamily.ENGLISH_BEBAS -> FontFamily(Font(R.font.bebas_neue, FontWeight.Normal))
            TextFontFamily.ENGLISH_PACIFICO -> FontFamily(Font(R.font.pacifico, FontWeight.Normal))
            TextFontFamily.ENGLISH_PLAYFAIR -> FontFamily(Font(R.font.playfair_display, FontWeight.Normal))
            TextFontFamily.ENGLISH_CINZEL -> FontFamily(Font(R.font.cinzel, FontWeight.Normal))
            TextFontFamily.SERIF -> FontFamily.Serif
            TextFontFamily.MONOSPACE -> FontFamily.Monospace
            TextFontFamily.CURSIVE -> FontFamily.Cursive
            TextFontFamily.BOLD_DISPLAY -> FontFamily.SansSerif
            TextFontFamily.MODERN_CLEAN -> FontFamily.SansSerif
            TextFontFamily.DEFAULT -> FontFamily.Default
        }
    }

    fun getNativeTypeface(context: Context, font: TextFontFamily): Typeface {
        return try {
            when (font) {
                TextFontFamily.HINDI_ROZHA -> ResourcesCompat.getFont(context, R.font.rozha_one) ?: Typeface.DEFAULT_BOLD
                TextFontFamily.HINDI_KALAM -> ResourcesCompat.getFont(context, R.font.kalam) ?: Typeface.DEFAULT
                TextFontFamily.HINDI_YATRA -> ResourcesCompat.getFont(context, R.font.yatra_one) ?: Typeface.DEFAULT_BOLD
                TextFontFamily.ENGLISH_BEBAS -> ResourcesCompat.getFont(context, R.font.bebas_neue) ?: Typeface.DEFAULT_BOLD
                TextFontFamily.ENGLISH_PACIFICO -> ResourcesCompat.getFont(context, R.font.pacifico) ?: Typeface.DEFAULT
                TextFontFamily.ENGLISH_PLAYFAIR -> ResourcesCompat.getFont(context, R.font.playfair_display) ?: Typeface.SERIF
                TextFontFamily.ENGLISH_CINZEL -> ResourcesCompat.getFont(context, R.font.cinzel) ?: Typeface.SERIF
                TextFontFamily.SERIF -> Typeface.SERIF
                TextFontFamily.MONOSPACE -> Typeface.MONOSPACE
                TextFontFamily.CURSIVE -> Typeface.create("casual", Typeface.NORMAL)
                TextFontFamily.BOLD_DISPLAY -> Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                TextFontFamily.MODERN_CLEAN -> Typeface.create("sans-serif-medium", Typeface.NORMAL)
                TextFontFamily.DEFAULT -> Typeface.DEFAULT
            }
        } catch (e: Exception) {
            Typeface.DEFAULT
        }
    }
}
