package com.example.data.preferences

import org.junit.Assert.assertEquals
import org.junit.Test

/** Formule de saturation et bornes du curseur « Couleur du verre » (JVM pure). */
class GlassColorStrengthTest {

    @Test
    fun defaultStrengthIsSeventyPercent() {
        assertEquals(70, DefaultGlassColorStrength)
        assertEquals(70, resolveGlassColorStrength(null))
    }

    @Test
    fun saturationFollowsFormula() {
        // 0,6 + 0,8 x force
        assertEquals(0.6f, glassSaturation(0), 1e-6f)
        assertEquals(1.0f, glassSaturation(50), 1e-6f)
        assertEquals(1.16f, glassSaturation(70), 1e-6f)
        assertEquals(1.4f, glassSaturation(100), 1e-6f)
    }

    @Test
    fun valuesOutsideRangeAreClamped() {
        assertEquals(0, clampGlassColorStrength(-20))
        assertEquals(100, clampGlassColorStrength(250))
        assertEquals(0, resolveGlassColorStrength(-1))
        assertEquals(100, resolveGlassColorStrength(101))
        assertEquals(0.6f, glassSaturation(-5), 1e-6f)
        assertEquals(1.4f, glassSaturation(500), 1e-6f)
    }
}
