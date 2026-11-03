package com.example.ui.adaptive

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Mise à jour 085 — seuils de mise en page adaptative : rail latéral dès Medium, deux volets
 * uniquement en Expanded.
 */
class WindowSizeClassTest {

    @Test
    fun `le téléphone reste en navigation basse`() {
        assertFalse(widthSupportsSideRail(WindowWidthSizeClass.Compact))
    }

    @Test
    fun `la tablette étroite passe au rail latéral`() {
        assertTrue(widthSupportsSideRail(WindowWidthSizeClass.Medium))
        assertTrue(widthSupportsSideRail(WindowWidthSizeClass.Expanded))
    }

    @Test
    fun `les deux volets sont réservés aux grandes largeurs`() {
        assertFalse(widthSupportsTwoPane(WindowWidthSizeClass.Compact))
        assertFalse(widthSupportsTwoPane(WindowWidthSizeClass.Medium))
        assertTrue(widthSupportsTwoPane(WindowWidthSizeClass.Expanded))
    }
}
