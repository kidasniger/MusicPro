package com.example.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.R
import com.example.data.preferences.AppThemeMode
import com.example.ui.theme.MusicProTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Mise à jour 088 — bandeau « Hors connexion » : visible seulement sans Internet, avec un
 * bouton « Réessayer » fonctionnel.
 */
@RunWith(RobolectricTestRunner::class)
class OfflineBannerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `le bandeau s affiche hors connexion avec un bouton réessayer`() {
        var retried = false
        composeTestRule.setContent {
            MusicProTheme(themeMode = AppThemeMode.DARK) {
                OfflineBanner(isOnline = false, onRetry = { retried = true })
            }
        }
        composeTestRule.onNodeWithTag("offline_banner").assertIsDisplayed()
        val retryLabel = ApplicationProvider.getApplicationContext<android.content.Context>()
            .getString(R.string.action_retry)
        composeTestRule.onNodeWithText(retryLabel).assertIsDisplayed().performClick()
        assertEquals(true, retried)
    }

    @Test
    fun `le bandeau disparaît une fois en ligne`() {
        composeTestRule.setContent {
            MusicProTheme(themeMode = AppThemeMode.DARK) {
                OfflineBanner(isOnline = true, onRetry = {})
            }
        }
        assertEquals(
            0,
            composeTestRule.onAllNodesWithTag("offline_banner").fetchSemanticsNodes().size
        )
    }
}
