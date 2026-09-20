package com.example.ui.settings

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.data.preferences.AppThemeMode
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.security.GroqApiKeyStore
import com.example.ui.theme.MusicProTheme
import com.example.util.CacheManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SettingsScreenRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testUserPreferencesThemeModePersistence() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = UserPreferencesRepository(context)

        // Set to DARK and verify
        repo.setThemeMode(AppThemeMode.DARK)
        assertEquals(AppThemeMode.DARK, repo.themeMode.first())

        // Set to LIGHT and verify
        repo.setThemeMode(AppThemeMode.LIGHT)
        assertEquals(AppThemeMode.LIGHT, repo.themeMode.first())

        // Set to SYSTEM and verify
        repo.setThemeMode(AppThemeMode.SYSTEM)
        assertEquals(AppThemeMode.SYSTEM, repo.themeMode.first())

        // Dynamic Color test
        repo.setDynamicColor(false)
        assertFalse(repo.isDynamicColorEnabled.first())
        repo.setDynamicColor(true)
        assertTrue(repo.isDynamicColorEnabled.first())
    }

    @Test
    fun testCacheManagerCalculationAndClear() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Create a dummy file in cache
        val testCacheFile = File(context.cacheDir, "test_album_art.cache")
        testCacheFile.writeText("Dummy cache data for MusicPro audio and images")

        val sizeFormattedBefore = CacheManager.getFormattedCacheSize(context)
        assertNotNull(sizeFormattedBefore)

        val success = CacheManager.clearCache(context)
        assertTrue(success)

        val sizeFormattedAfter = CacheManager.getFormattedCacheSize(context)
        assertEquals("0 Ko", sizeFormattedAfter)
    }

    @Test
    fun testGroqApiKeyStoreNeverFallsBackToPlaintextStorage() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val keyStore = GroqApiKeyStore.getInstance(context)

        val sampleKey = "gsk_test1234567890abcdefghijklmnopqrstuvwxyz"
        val saved = keyStore.setApiKey(sampleKey)

        if (saved) {
            assertTrue(keyStore.hasApiKey())
            assertEquals(sampleKey, keyStore.getApiKey())
            assertTrue(keyStore.getMaskedApiKey().startsWith("gsk_"))
            assertTrue(keyStore.getMaskedApiKey().contains("••••"))

            keyStore.clearApiKey()
            assertFalse(keyStore.hasApiKey())
            assertEquals("", keyStore.getApiKey())
        } else {
            // Robolectric peut ne pas fournir un Keystore complet :
            // l'échec est acceptable tant qu'aucune clé n'est persistée en clair.
            assertFalse(keyStore.hasApiKey())
            val plaintextFallback = context.getSharedPreferences(
                "musicpro_groq_fallback_prefs",
                Context.MODE_PRIVATE
            )
            assertFalse(plaintextFallback.contains("key_groq_whisper_api"))
        }
    }
    @Test
    fun testSettingsScreenComposesSuccessfully() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = SettingsViewModel(app)

        composeTestRule.setContent {
            MusicProTheme(themeMode = AppThemeMode.DARK) {
                SettingsScreen(
                    onBack = {},
                    onNavigateToPermissions = {},
                    settingsViewModel = viewModel
                )
            }
        }

        // Verify key sections are present
        composeTestRule.onNodeWithTag("theme_settings_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("groq_settings_card").assertIsDisplayed()

        // Verify Theme buttons
        composeTestRule.onNodeWithTag("theme_dark_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("theme_light_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("theme_system_button").assertIsDisplayed()

        // Click on Light Theme
        composeTestRule.onNodeWithTag("theme_light_button").performClick()

        // Scroll to and verify Cache Card
        composeTestRule.onNodeWithTag("cache_settings_card").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("about_settings_card").performScrollTo().assertIsDisplayed()

        // Click on Clear Cache button to trigger dialog
        composeTestRule.onNodeWithTag("clear_cache_button").performScrollTo().assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("confirm_clear_cache_button").assertIsDisplayed().performClick()
    }
}
