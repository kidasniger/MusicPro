package com.example.ui.a11y

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.local.AudioTrackEntity
import com.example.data.preferences.AppThemeMode
import com.example.ui.components.glass.GlassMiniPlayer
import com.example.ui.library.TrackRowItem
import com.example.ui.theme.MusicProTheme
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Locale

/**
 * Mise à jour 081 — audit des cibles tactiles : parcourt l'arbre de sémantique des composants
 * clés et signale tout élément cliquable dont la zone tactile est inférieure à 48×48 dp.
 *
 * Les IconButton Material3 élargissent déjà leur zone à 48 dp (taille minimale imposée) ; les
 * cibles personnalisées doivent utiliser `Modifier.minimumInteractiveComponentSize()`.
 */
@RunWith(RobolectricTestRunner::class)
class TouchTargetAuditTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun sampleTrack(id: Long = 1L) = AudioTrackEntity(
        id = id,
        title = "Morceau $id",
        artist = "Artiste $id",
        album = "Album $id",
        duration = 215_000L,
        contentUri = "content://media/audio/$id"
    )

    /** Renvoie les nœuds cliquables dont la ZONE TACTILE est inférieure à [minDp] dp. */
    private fun tooSmallTargets(minDp: Float): List<Pair<SemanticsNode, Float>> {
        val density = composeTestRule.density.density
        val result = mutableListOf<Pair<SemanticsNode, Float>>()
        fun visit(node: SemanticsNode) {
            if (node.config.contains(SemanticsActions.OnClick)) {
                // touchBoundsInRoot inclut l'agrandissement de zone tactile de Material 3
                // (taille minimale 48 dp) : c'est la vraie surface touchée par le doigt.
                val bounds: Rect = node.touchBoundsInRoot
                val smallest = minOf(bounds.width, bounds.height) / density
                if (smallest < minDp) result += node to smallest
            }
            node.children.forEach { visit(it) }
        }
        visit(composeTestRule.onRoot().fetchSemanticsNode())
        return result
    }

    private fun assertAllTargetsAtLeast(minDp: Float, screenName: String) {
        val offenders = tooSmallTargets(minDp)
        val detail = offenders.joinToString(separator = "; ") { (node, size) ->
            String.format(
                Locale.FRENCH,
                "%.1f dp",
                size
            )
        }
        assertTrue(
            "$screenName : ${offenders.size} cible(s) tactile(s) < ${minDp.toInt()} dp [$detail]",
            offenders.isEmpty()
        )
    }

    @Test
    fun `ligne de morceau - toutes les cibles atteignent 48 dp`() {
        composeTestRule.setContent {
            MusicProTheme(themeMode = AppThemeMode.DARK) {
                TrackRowItem(
                    index = 1,
                    track = sampleTrack(),
                    isCurrent = true,
                    isPlaying = true,
                    isFavorite = false,
                    onClick = {},
                    onToggleFavorite = {},
                    onOpenActions = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        assertAllTargetsAtLeast(48f, "TrackRowItem")
    }

    @Test
    fun `mini lecteur - toutes les cibles atteignent 48 dp`() {
        composeTestRule.setContent {
            MusicProTheme(themeMode = AppThemeMode.DARK) {
                GlassMiniPlayer(
                    visible = true,
                    track = sampleTrack(2),
                    isPlaying = true,
                    onPlayPauseToggle = {},
                    onNext = {},
                    onPrevious = {},
                    onOpen = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        assertAllTargetsAtLeast(48f, "GlassMiniPlayer")
    }
}
