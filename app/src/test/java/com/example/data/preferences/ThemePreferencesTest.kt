package com.example.data.preferences

import com.example.ui.theme.resolveIsDark
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Migration de `app_theme_mode` (013) et plage « Sombre le soir » (018). */
class ThemePreferencesTest {

    @Test
    fun legacy_values_are_migrated() {
        assertEquals(AppearanceMode.DARK, legacyThemeToAppearance("DARK"))
        assertEquals(AppStyle.NEON, legacyThemeToStyle("DARK"))
        assertEquals(AppearanceMode.LIGHT, legacyThemeToAppearance("LIGHT"))
        assertEquals(AppStyle.MINIMAL, legacyThemeToStyle("LIGHT"))
        assertEquals(AppearanceMode.AUTO, legacyThemeToAppearance("SYSTEM"))
        assertEquals(AppStyle.MINIMAL, legacyThemeToStyle("SYSTEM"))
    }

    @Test
    fun missing_or_invalid_values_fall_back_safely() {
        assertEquals(AppearanceMode.DARK, resolveAppearanceMode(null, null))
        assertEquals(AppStyle.NEON, resolveAppStyle("inconnu", null))
        assertEquals(AppearanceMode.AUTO, resolveAppearanceMode(null, "SYSTEM"))
        assertEquals(AccentChoice.TERRACOTTA, resolveAccentChoice(null))
        assertEquals(AppStyle.GLASS, resolveAppStyle("GLASS", "LIGHT"))
    }

    @Test
    fun evening_window_crosses_midnight() {
        val start = DefaultEveningStartMinutes
        val end = DefaultEveningEndMinutes
        assertTrue(isInDarkWindow(21 * 60, start, end))
        assertTrue(isInDarkWindow(6 * 60, start, end))
        assertFalse(isInDarkWindow(12 * 60, start, end))
        assertFalse(isInDarkWindow(7 * 60, start, end))
        assertFalse(isInDarkWindow(10 * 60, 10 * 60, 10 * 60))
    }

    @Test
    fun resolved_darkness_follows_the_chosen_appearance() {
        assertTrue(resolveIsDark(AppearanceMode.DARK, eveningDark = false, systemDark = false))
        assertFalse(resolveIsDark(AppearanceMode.LIGHT, eveningDark = true, systemDark = true))
        assertTrue(resolveIsDark(AppearanceMode.AUTO, eveningDark = false, systemDark = true))
        assertTrue(resolveIsDark(AppearanceMode.DARK_EVENING, eveningDark = true, systemDark = false))
        assertFalse(resolveIsDark(AppearanceMode.DARK_EVENING, eveningDark = false, systemDark = true))
    }
}
