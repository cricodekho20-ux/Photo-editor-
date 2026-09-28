package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.DefaultTemplates
import com.example.model.BackgroundType
import com.example.model.ShapeType
import com.example.model.TextFontFamily
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches PixCraft Pro`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("PixCraft Pro", appName)
    }

    @Test
    fun `starter templates are loaded with layers`() {
        val templates = DefaultTemplates.getTemplates()
        assertTrue(templates.isNotEmpty())
        val megaSale = templates.find { it.id == "template_mega_sale" }
        assertNotNull(megaSale)
        assertEquals(BackgroundType.GRADIENT, megaSale?.backgroundConfig?.type)
        assertTrue(megaSale!!.layers.isNotEmpty())
    }
}
