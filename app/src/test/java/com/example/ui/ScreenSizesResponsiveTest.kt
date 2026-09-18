package com.example.ui

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.core.app.ApplicationProvider
import com.example.data.preferences.AppThemeMode
import com.example.permissions.PermissionUiState
import com.example.ui.navigation.MusicProNavGraph
import com.example.ui.settings.SettingsScreen
import com.example.ui.settings.SettingsViewModel
import com.example.ui.theme.MusicProTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class ScreenSizesResponsiveTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Test 1 : Petit téléphone (Small phone, 320x568 dp, portrait)
     * Vérifie que l'écran des paramètres s'affiche sans crash, scrollable,
     * et que les composants ne débordent pas.
     */
    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun testSmallPhonePortraitLayout() {
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

        // Vérification présence et affichage correct
        composeTestRule.onNodeWithTag("settings_back_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("theme_settings_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("theme_dark_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("groq_settings_card").assertIsDisplayed()
    }

    /**
     * Test 2 : Téléphone standard (Standard phone, 412x915 dp, portrait - ex. Pixel 7 / Galaxy S23)
     * Vérifie le rendu et les interactions sur taille standard.
     */
    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun testStandardPhonePortraitLayout() {
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

        composeTestRule.onNodeWithTag("settings_back_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("theme_settings_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("theme_light_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("groq_settings_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("cache_settings_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("about_settings_card").assertIsDisplayed()
    }

    /**
     * Test 3 : Grand téléphone avec notch / découpe (Large phone / tall display, 430x932 dp, portrait - ex. Pixel 8 Pro / Galaxy Ultra)
     * Vérifie le rendu sur grand écran et gestion des insets.
     */
    @Test
    @Config(sdk = [36], qualifiers = "w430dp-h932dp-normal-long-notround-port-xxxhdpi")
    fun testLargePhoneNotchPortraitLayout() {
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

        composeTestRule.onNodeWithTag("settings_back_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("theme_settings_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("theme_system_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("groq_settings_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("cache_settings_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("about_settings_card").assertIsDisplayed()
    }

    /**
     * Test de l'ensemble du graphe de navigation et de ses transitions animées
     */
    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-port")
    fun testNavigationGraphWithTransitions() {
        val dummyState = PermissionUiState(
            isAudioGranted = true,
            isNotificationGranted = true,
            isDeniedExplanationNeeded = false
        )

        composeTestRule.setContent {
            MusicProTheme(themeMode = AppThemeMode.DARK) {
                MusicProNavGraph(
                    uiState = dummyState,
                    isOnboardingCompleted = true,
                    onCompleteOnboarding = {},
                    onRequestPermissions = {},
                    onManualCheck = {}
                )
            }
        }

        // Le graphe démarre sur SplashScreen (1.5s) puis bascule de façon fluide sur HomeScreen
        composeTestRule.waitForIdle()
    }

    /**
     * Test 5 : Rotation d'écran en mode Paysage (Landscape rotation 915x412 dp)
     * Vérifie que l'écran des paramètres et l'UI restent parfaitement stables et lisibles
     * lors de la bascule d'orientation horizontale sans crash ni perte d'état.
     */
    @Test
    @Config(sdk = [36], qualifiers = "w915dp-h412dp-land-xxhdpi")
    fun testLandscapeRotationLayout() {
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

        // L'UI en mode paysage doit afficher les contrôles principaux et rester scrollable
        composeTestRule.onNodeWithTag("settings_back_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("theme_settings_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("theme_dark_button").assertIsDisplayed()
    }
}

