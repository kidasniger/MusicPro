package com.example.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Mise à jour 074 — feuilles du bas à ancrages : deux ancrages (mi-hauteur, plein
 * écran) plus la fermeture, poignée 36×4 dp dans une zone de 48 dp.
 */
class AnchoredSheetSpecTest {

    @Test
    fun `les ancrages sont mi-hauteur et plein écran`() {
        assertEquals(0.55f, AnchoredSheetSpec.HALF_FRACTION, 0.0001f)
        assertEquals(0.9f, AnchoredSheetSpec.FULL_THRESHOLD, 0.0001f)
        assertTrue(AnchoredSheetSpec.HALF_FRACTION < AnchoredSheetSpec.FULL_THRESHOLD)
    }

    @Test
    fun `poignée 36x4 dp dans une zone de 48 dp`() {
        assertEquals(36, AnchoredSheetSpec.HANDLE_WIDTH_DP)
        assertEquals(4, AnchoredSheetSpec.HANDLE_HEIGHT_DP)
        assertEquals(48, AnchoredSheetSpec.HANDLE_ZONE_DP)
    }

    @Test
    fun `relâchement sans vitesse revient à l'ancrage le plus proche`() {
        assertEquals(1f, AnchoredSheetSpec.nearestAnchor(0.9f, 0f), 0.0001f)
        assertEquals(0f, AnchoredSheetSpec.nearestAnchor(0.1f, 0f), 0.0001f)
        assertEquals(
            AnchoredSheetSpec.HALF_FRACTION,
            AnchoredSheetSpec.nearestAnchor(AnchoredSheetSpec.HALF_FRACTION + 0.05f, 0f),
            0.0001f
        )
    }

    @Test
    fun `un geste rapide vers le bas ferme la feuille`() {
        assertEquals(0f, AnchoredSheetSpec.nearestAnchor(0.9f, -3f), 0.0001f)
        assertEquals(0f, AnchoredSheetSpec.nearestAnchor(AnchoredSheetSpec.HALF_FRACTION, -1.5f), 0.0001f)
    }

    @Test
    fun `un geste rapide vers le haut ouvre la feuille en plein écran`() {
        assertEquals(1f, AnchoredSheetSpec.nearestAnchor(0.1f, 3f), 0.0001f)
        assertEquals(1f, AnchoredSheetSpec.nearestAnchor(AnchoredSheetSpec.HALF_FRACTION, 1.5f), 0.0001f)
    }
}
