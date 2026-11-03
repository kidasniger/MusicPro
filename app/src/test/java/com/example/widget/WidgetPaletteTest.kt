package com.example.widget

import androidx.compose.ui.graphics.toArgb
import com.example.data.preferences.AppearanceMode
import com.example.data.preferences.AppStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Mise à jour 080 — palette du widget alignée sur le style choisi
 * (clair / sombre / verre).
 */
class WidgetPaletteTest {

    @Test
    fun `style clair`() {
        val palette = WidgetPalette.forAppearance(AppearanceMode.LIGHT, isGlass = false, isNight = false)
        assertEquals(WidgetPalette.LIGHT, palette)
        assertEquals(0xFFF4F5F8.toInt(), palette.background.toArgb())
    }

    @Test
    fun `style sombre et sombre le soir`() {
        assertEquals(
            WidgetPalette.DARK,
            WidgetPalette.forAppearance(AppearanceMode.DARK, isGlass = false, isNight = false)
        )
        assertEquals(
            WidgetPalette.DARK,
            WidgetPalette.forAppearance(AppearanceMode.DARK_EVENING, isGlass = false, isNight = false)
        )
    }

    @Test
    fun `style verre translucide prioritaire`() {
        val glass = WidgetPalette.forAppearance(AppearanceMode.LIGHT, isGlass = true, isNight = false)
        assertEquals(WidgetPalette.GLASS, glass)
        // Alpha translucide (< FF) sur le canal alpha.
        assertNotEquals(0xFF, glass.background.toArgb() ushr 24)
    }

    @Test
    fun `mode auto suit le système`() {
        assertEquals(
            WidgetPalette.LIGHT,
            WidgetPalette.forAppearance(AppearanceMode.AUTO, isGlass = false, isNight = false)
        )
        assertEquals(
            WidgetPalette.DARK,
            WidgetPalette.forAppearance(AppearanceMode.AUTO, isGlass = false, isNight = true)
        )
    }
}
