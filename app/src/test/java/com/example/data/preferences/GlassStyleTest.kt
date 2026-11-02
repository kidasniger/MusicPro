package com.example.data.preferences

import org.junit.Assert.assertEquals
import org.junit.Test

/** Tests des règles pures des curseurs « Film » et « Onglet actif » (mise à jour 009). */
class GlassStyleTest {

    @Test
    fun defaultsWhenNothingStored() {
        assertEquals(50, resolveGlassFilmOpacity(null))
        assertEquals(26, resolveGlassActiveTabOpacity(null))
    }

    @Test
    fun storedValuesAreClampedToZeroHundred() {
        assertEquals(0, resolveGlassFilmOpacity(-5))
        assertEquals(100, resolveGlassFilmOpacity(250))
        assertEquals(0, resolveGlassActiveTabOpacity(-1))
        assertEquals(100, resolveGlassActiveTabOpacity(101))
        assertEquals(37, resolveGlassActiveTabOpacity(37))
    }

    @Test
    fun neutralFilmLeavesRecipeUnchanged() {
        assertEquals(0.55f, glassScaledFilmAlpha(0.55f, 50), 0.0001f)
        assertEquals(0.86f, glassScaledFilmAlpha(0.86f, 50), 0.0001f)
    }

    @Test
    fun zeroFilmIsTransparent() {
        assertEquals(0f, glassScaledFilmAlpha(0.86f, 0), 0.0001f)
    }

    @Test
    fun maxFilmDoublesAndIsCappedAtOne() {
        assertEquals(0.9f, glassScaledFilmAlpha(0.45f, 100), 0.0001f)
        assertEquals(1f, glassScaledFilmAlpha(0.86f, 100), 0.0001f)
    }
}
