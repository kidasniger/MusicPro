package com.example.ui.components

import com.example.ui.components.glass.COVER_TRANSITION_MS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Constantes de mouvement des mises à jour 072/075/078 — gardées au point par les
 * tests pour éviter les régressions de ressenti.
 */
class UiMotionSpecTest {

    @Test
    fun `transition de pochette 320 ms`() {
        assertEquals(320, COVER_TRANSITION_MS)
    }

    @Test
    fun `micro-interaction de pression 0,97 - 80 ms - ressort 0,7`() {
        assertEquals(0.97f, PressFeedbackSpec.PRESSED_SCALE, 0.0001f)
        assertEquals(80, PressFeedbackSpec.PRESS_IN_MS)
        assertEquals(0.7f, PressFeedbackSpec.RETURN_DAMPING, 0.0001f)
        assertEquals(0.08f, PressFeedbackSpec.VEIL_ALPHA, 0.0001f)
    }

    @Test
    fun `révélation de thème 400 ms et repli 250 ms`() {
        assertEquals(400, ThemeRevealSpec.CIRCLE_MS)
        assertEquals(250, ThemeRevealSpec.FADE_MS)
        assertTrue(ThemeRevealSpec.FADE_MS < ThemeRevealSpec.CIRCLE_MS)
    }
}
