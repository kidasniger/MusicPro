package com.example.util

import com.example.playback.MusicPlaybackService
import com.example.util.ShortcutActions.ShortcutRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Mise à jour 080 — analyse des actions des raccourcis d'application.
 */
class ShortcutActionsTest {

    @Test
    fun `lecture aléatoire reconnue`() {
        assertEquals(
            ShortcutRequest.SHUFFLE_PLAY,
            ShortcutActions.parse(ShortcutActions.ACTION_SHUFFLE_PLAY)
        )
    }

    @Test
    fun `recherche reconnue`() {
        assertEquals(
            ShortcutRequest.OPEN_SEARCH,
            ShortcutActions.parse(ShortcutActions.ACTION_OPEN_SEARCH)
        )
    }

    @Test
    fun `dernière lecture réutilise l'action du service de lecture`() {
        assertEquals(
            ShortcutRequest.OPEN_LAST_PLAYED,
            ShortcutActions.parse(MusicPlaybackService.ACTION_SHOW_NOW_PLAYING)
        )
    }

    @Test
    fun `lancement normal sans action renvoie null`() {
        assertNull(ShortcutActions.parse(null))
        assertNull(ShortcutActions.parse("android.intent.action.MAIN"))
        assertNull(ShortcutActions.parse("action.inconnue"))
    }

    @Test
    fun `les actions publiées correspondent aux intents du fichier shortcuts_xml`() {
        // Ces valeurs sont référencées par res/xml/shortcuts.xml — tout changement
        // doit être répercuté dans les deux endroits.
        assertEquals("com.example.musicpro.action.SHUFFLE_PLAY", ShortcutActions.ACTION_SHUFFLE_PLAY)
        assertEquals("com.example.musicpro.action.OPEN_SEARCH", ShortcutActions.ACTION_OPEN_SEARCH)
    }
}
