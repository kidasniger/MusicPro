package com.example.data.preferences

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Bornes, défaut et seuil de désactivation du curseur « Givre » (JVM pure). */
class GlassFrostTest {

    @Test
    fun defaultFrostIsSixDp() {
        assertEquals(6, DefaultGlassFrostDp)
        assertEquals(6, resolveGlassFrostDp(null))
    }

    @Test
    fun valuesOutsideRangeAreClamped() {
        assertEquals(0, clampGlassFrostDp(-3))
        assertEquals(24, clampGlassFrostDp(40))
        assertEquals(0, resolveGlassFrostDp(-1))
        assertEquals(24, resolveGlassFrostDp(25))
        assertEquals(12, resolveGlassFrostDp(12))
    }

    @Test
    fun blurIsOffBelowTwoDp() {
        assertFalse(glassBlurActive(GlassMaterial.BALANCED, blurSupported = true, frostDp = 0))
        assertFalse(glassBlurActive(GlassMaterial.BALANCED, blurSupported = true, frostDp = 1))
        assertTrue(glassBlurActive(GlassMaterial.BALANCED, blurSupported = true, frostDp = 2))
        assertTrue(glassBlurActive(GlassMaterial.QUALITY, blurSupported = true, frostDp = 24))
    }

    @Test
    fun fastestOrUnsupportedDeviceNeverBlurs() {
        assertFalse(glassBlurActive(GlassMaterial.FASTEST, blurSupported = true, frostDp = 24))
        assertFalse(glassBlurActive(GlassMaterial.BALANCED, blurSupported = false, frostDp = 24))
        assertFalse(glassBlurActive(GlassMaterial.QUALITY, blurSupported = false, frostDp = 24))
    }
}
