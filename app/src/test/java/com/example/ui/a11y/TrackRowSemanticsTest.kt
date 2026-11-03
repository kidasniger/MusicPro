package com.example.ui.a11y

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import com.example.data.local.AudioTrackEntity
import com.example.data.preferences.AppThemeMode
import com.example.ui.library.TrackRowItem
import com.example.ui.theme.MusicProTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Mise à jour 082 — TalkBack : la ligne de morceau fusionne ses descendants (titre, artiste,
 * durée lus d'un bloc) et annonce « En cours de lecture » quand le morceau est actif.
 */
@RunWith(RobolectricTestRunner::class)
class TrackRowSemanticsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun sampleTrack() = AudioTrackEntity(
        id = 42L,
        title = "Nuit électrique",
        artist = "Étoile Froide",
        album = "Album Test",
        duration = 215_000L,
        contentUri = "content://media/audio/42"
    )

    @Test
    fun `la ligne active annonce en cours de lecture`() {
        val track = sampleTrack()
        composeTestRule.setContent {
            MusicProTheme(themeMode = AppThemeMode.DARK) {
                Column(Modifier.fillMaxWidth()) {
                    TrackRowItem(
                        index = 1,
                        track = track,
                        isCurrent = true,
                        isPlaying = true,
                        isFavorite = false,
                        onClick = {},
                        onToggleFavorite = {},
                        modifier = Modifier.testTag("row_under_test")
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
        val state = composeTestRule
            .onNodeWithTag("row_under_test", useUnmergedTree = true)
            .fetchSemanticsNode()
            .config.getOrNull(SemanticsProperties.StateDescription)
        assertEquals("En cours de lecture", state)
    }

    @Test
    fun `la ligne inactive n annonce pas d état de lecture`() {
        val track = sampleTrack()
        composeTestRule.setContent {
            MusicProTheme(themeMode = AppThemeMode.DARK) {
                Column(Modifier.fillMaxWidth()) {
                    TrackRowItem(
                        index = 2,
                        track = track,
                        isCurrent = false,
                        isPlaying = false,
                        isFavorite = false,
                        onClick = {},
                        onToggleFavorite = {},
                        modifier = Modifier.testTag("row_under_test")
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
        val state = composeTestRule
            .onNodeWithTag("row_under_test", useUnmergedTree = true)
            .fetchSemanticsNode()
            .config.getOrNull(SemanticsProperties.StateDescription)
        assertEquals(null, state)
    }
}
