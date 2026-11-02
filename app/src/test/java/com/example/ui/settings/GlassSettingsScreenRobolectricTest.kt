package com.example.ui.settings

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.data.preferences.AppThemeMode
import com.example.data.preferences.GlassMaterial
import com.example.ui.theme.MusicProTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Mise à jour 025 : écran `settings/glass`. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GlassSettingsScreenRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun glassScreenShowsPreviewAndBothSections() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = SettingsViewModel(app)

        composeTestRule.setContent {
            MusicProTheme(themeMode = AppThemeMode.DARK) {
                GlassSettingsScreen(onBack = {}, settingsViewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithTag("glass_settings_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("glass_back_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("glass_preview").assertIsDisplayed()
        composeTestRule.onNodeWithTag("glass_quality_card").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("glass_tint_card").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("glass_film_card").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("glass_reset_button").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun backButtonCallsOnBack() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = SettingsViewModel(app)
        var backCount = 0

        composeTestRule.setContent {
            MusicProTheme(themeMode = AppThemeMode.LIGHT, dynamicColor = false) {
                GlassSettingsScreen(onBack = { backCount++ }, settingsViewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithTag("glass_back_button").performClick()
        assertTrue(backCount == 1)
    }

    @Test
    fun resetButtonRestoresDefaults() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = SettingsViewModel(app)

        composeTestRule.setContent {
            MusicProTheme(themeMode = AppThemeMode.DARK) {
                GlassSettingsScreen(onBack = {}, settingsViewModel = viewModel)
            }
        }

        viewModel.setGlassMaterial(GlassMaterial.FASTEST)
        viewModel.setGlassFrostDp(12)
        viewModel.setGlassTintHue(200f)
        composeTestRule.waitForIdle()
        assertEquals(GlassMaterial.FASTEST, viewModel.glassPreview.value.material)

        composeTestRule.onNodeWithTag("glass_reset_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        assertEquals(GlassPreviewState(), viewModel.glassPreview.value)
    }
}
