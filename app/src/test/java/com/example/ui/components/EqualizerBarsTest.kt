package com.example.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Mise à jour 076 — math de l'égaliseur : 3 barres, hauteurs 30–100 %, phases
 * décalées de 120 ms, niveaux figés à la pause.
 */
class EqualizerBarsTest {

    @Test
    fun `trois barres`() {
        assertEquals(3, EqualizerBars.BAR_COUNT)
        assertEquals(3, EqualizerBars.frozenLevels().size)
    }

    @Test
    fun `décalage de 120 ms entre les barres`() {
        assertEquals(120, EqualizerBars.STAGGER_MS)
    }

    @Test
    fun `les niveaux restent entre 30 et 100 pourcents`() {
        for (bar in 0 until EqualizerBars.BAR_COUNT) {
            for (step in 0..100) {
                val t = step / 100f
                val level = EqualizerBars.levelFor(bar, t)
                assertTrue(
                    "barre $bar t=$t niveau=$level",
                    level in EqualizerBars.MIN_LEVEL..EqualizerBars.MAX_LEVEL
                )
            }
        }
    }

    @Test
    fun `la barre 0 atteint les deux bornes sur une période`() {
        val levels = (0..100).map { EqualizerBars.levelFor(0, it / 100f) }
        assertTrue(levels.min() <= EqualizerBars.MIN_LEVEL + 0.01f)
        assertTrue(levels.max() >= EqualizerBars.MAX_LEVEL - 0.01f)
    }

    @Test
    fun `les niveaux figés de la pause sont plausibles`() {
        val frozen = EqualizerBars.frozenLevels()
        assertTrue(frozen.all { it in EqualizerBars.MIN_LEVEL..EqualizerBars.MAX_LEVEL })
        assertTrue(frozen[0] != frozen[1] || frozen[1] != frozen[2])
    }
}
