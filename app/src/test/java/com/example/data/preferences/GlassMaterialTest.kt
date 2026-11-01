package com.example.data.preferences

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GlassMaterialTest {

    @Test
    fun storedValueWinsOverLegacy() {
        assertEquals(GlassMaterial.QUALITY, resolveGlassMaterial("QUALITY", "FAST"))
        assertEquals(GlassMaterial.FASTEST, resolveGlassMaterial("FASTEST", "BEAUTIFUL"))
    }

    @Test
    fun legacyKeyIsMigrated() {
        assertEquals(GlassMaterial.FASTEST, resolveGlassMaterial(null, "FAST"))
        assertEquals(GlassMaterial.QUALITY, resolveGlassMaterial(null, "BEAUTIFUL"))
    }

    @Test
    fun nothingStoredOrUnknownValuesGiveBalanced() {
        assertEquals(GlassMaterial.BALANCED, resolveGlassMaterial(null, null))
        assertEquals(GlassMaterial.BALANCED, resolveGlassMaterial("???", "???"))
    }

    @Test
    fun qualityIsNotOfferedWithoutBlurSupport() {
        assertEquals(GlassMaterial.BALANCED, GlassMaterial.QUALITY.effective(blurSupported = false))
        assertEquals(GlassMaterial.QUALITY, GlassMaterial.QUALITY.effective(blurSupported = true))
        assertEquals(GlassMaterial.FASTEST, GlassMaterial.FASTEST.effective(blurSupported = false))
        assertEquals(GlassMaterial.BALANCED, GlassMaterial.BALANCED.effective(blurSupported = false))
    }

    @Test
    fun onlyFastestNeverBlurs() {
        assertFalse(GlassMaterial.FASTEST.rendersBlur(blurSupported = true))
        assertTrue(GlassMaterial.BALANCED.rendersBlur(blurSupported = true))
        assertTrue(GlassMaterial.QUALITY.rendersBlur(blurSupported = true))
        assertFalse(GlassMaterial.BALANCED.rendersBlur(blurSupported = false))
        assertFalse(GlassMaterial.QUALITY.rendersBlur(blurSupported = false))
    }
}
