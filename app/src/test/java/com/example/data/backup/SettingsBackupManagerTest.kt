package com.example.data.backup

import com.example.data.preferences.AccentChoice
import com.example.data.preferences.AppearanceMode
import com.example.data.preferences.AppStyle
import com.example.data.preferences.GlassMaterial
import com.example.data.preferences.ListDensityPreference
import com.example.data.preferences.TextSizePreference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SettingsBackupManagerTest {

    private fun sample(): SettingsBackup = SettingsBackup(
        schemaVersion = BACKUP_SCHEMA_VERSION,
        appearance = AppearanceMode.DARK_EVENING,
        appStyle = AppStyle.GLASS,
        accentChoice = AccentChoice.VIOLET,
        eveningStartMinutes = 21 * 60,
        eveningEndMinutes = 6 * 60,
        dynamicColor = true,
        textSize = TextSizePreference.LARGE,
        listDensity = ListDensityPreference.COMPACT,
        reduceAnimations = true,
        glass = GlassBackup(
            material = GlassMaterial.QUALITY,
            colorStrength = 85,
            frostDp = 12,
            tintHue = 270f,
            filmOpacity = 40,
            activeTabOpacity = 30
        ),
        karaoke = KaraokeBackup(fontSize = 24f, activeColor = "violet", offsetMs = -250L),
        favorites = FavoritesBackup(trackIds = listOf(1L, 42L, 99L), artists = listOf("Moman Sani", "Zara G"))
    )

    @Test
    fun `aller-retour JSON conserve les réglages`() {
        val json = sample().toJson()
        val parsed = SettingsBackupManager.fromJson(json)

        assertEquals(BACKUP_SCHEMA_VERSION, parsed.schemaVersion)
        assertEquals(AppearanceMode.DARK_EVENING, parsed.appearance)
        assertEquals(AppStyle.GLASS, parsed.appStyle)
        assertEquals(AccentChoice.VIOLET, parsed.accentChoice)
        assertEquals(21 * 60, parsed.eveningStartMinutes)
        assertEquals(6 * 60, parsed.eveningEndMinutes)
        assertEquals(true, parsed.dynamicColor)
        assertEquals(TextSizePreference.LARGE, parsed.textSize)
        assertEquals(ListDensityPreference.COMPACT, parsed.listDensity)
        assertEquals(true, parsed.reduceAnimations)
        assertEquals(GlassMaterial.QUALITY, parsed.glass.material)
        assertEquals(85, parsed.glass.colorStrength)
        assertEquals(12, parsed.glass.frostDp)
        assertEquals(270f, parsed.glass.tintHue)
        assertEquals(40, parsed.glass.filmOpacity)
        assertEquals(30, parsed.glass.activeTabOpacity)
        assertEquals(24f, parsed.karaoke.fontSize)
        assertEquals("violet", parsed.karaoke.activeColor)
        assertEquals(-250L, parsed.karaoke.offsetMs)
        assertEquals(listOf(1L, 42L, 99L), parsed.favorites.trackIds)
        assertEquals(listOf("Moman Sani", "Zara G"), parsed.favorites.artists)
    }

    @Test
    fun `JSON invalide leve BackupFormatException`() {
        assertThrows(BackupFormatException::class.java) {
            SettingsBackupManager.fromJson("ceci n'est pas du json {")
        }
    }

    @Test
    fun `fichier etranger leve BackupFormatException`() {
        val json = "{\"schemaVersion\":1,\"app\":\"AutreApp\"}"
        assertThrows(BackupFormatException::class.java) {
            SettingsBackupManager.fromJson(json)
        }
    }

    @Test
    fun `schema plus recent refuse`() {
        val json = sample().toJson().replace("\"schemaVersion\": 1", "\"schemaVersion\": 99")
        val e = assertThrows(BackupFormatException::class.java) {
            SettingsBackupManager.fromJson(json)
        }
        assertEquals(true, e.message?.contains("plus récent"))
    }

    @Test
    fun `valeurs inconnues retombent sur les defauts`() {
        val json = """
            {"schemaVersion":1,"app":"MusicPro","appearance":"INCONNU","appStyle":"INCONNU",
             "accentChoice":"INCONNU","textSize":"INCONNU","listDensity":"INCONNU",
             "glass":{"material":"INCONNU","tintHue":null},
             "favorites":{"trackIds":[0,-5,7],"artists":["", "Kidal"]}}
        """.trimIndent()
        val parsed = SettingsBackupManager.fromJson(json)
        assertEquals(AppearanceMode.DARK, parsed.appearance)
        assertEquals(AppStyle.NEON, parsed.appStyle)
        assertEquals(AccentChoice.TERRACOTTA, parsed.accentChoice)
        assertEquals(TextSizePreference.NORMAL, parsed.textSize)
        assertEquals(ListDensityPreference.COMFORTABLE, parsed.listDensity)
        assertEquals(GlassMaterial.BALANCED, parsed.glass.material)
        assertNull(parsed.glass.tintHue)
        assertEquals(listOf(7L), parsed.favorites.trackIds)
        assertEquals(listOf("Kidal"), parsed.favorites.artists)
    }
}
