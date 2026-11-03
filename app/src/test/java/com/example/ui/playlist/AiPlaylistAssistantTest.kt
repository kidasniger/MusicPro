package com.example.ui.playlist

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Mise à jour 069 — parsing de la réponse IA : seuls les identifiants réels
 * (retrouvés ensuite dans la bibliothèque) peuvent devenir des morceaux.
 */
class AiPlaylistAssistantTest {

    @Test
    fun `les identifiants sont extraits d'une liste à virgules`() {
        assertEquals(listOf(12L, 7L, 42L), AiPlaylistAssistant.parseTrackIds("12, 7, 42"))
    }

    @Test
    fun `le parsing tolère puces et libellés`() {
        val response = """
            Voici ma sélection :
            - ID=31 : Neon Nights
            * 8
            [55]
        """.trimIndent()
        assertEquals(listOf(31L, 8L, 55L), AiPlaylistAssistant.parseTrackIds(response))
    }

    @Test
    fun `les doublons sont dédupliqués`() {
        assertEquals(listOf(5L, 9L), AiPlaylistAssistant.parseTrackIds("5, 9, 5, 05"))
    }

    @Test
    fun `une réponse vide ne donne rien`() {
        assertTrue(AiPlaylistAssistant.parseTrackIds("").isEmpty())
        assertTrue(AiPlaylistAssistant.parseTrackIds("aucun identifiant ici").isEmpty())
    }

    @Test
    fun `l'invite impose le catalogue local et l'absence d'invention`() {
        val prompt = AiPlaylistAssistant.buildPrompt("nuit synthétique", "1 | A | X | Al | Synth")
        assertTrue(prompt.contains("UNIQUEMENT"))
        assertTrue(prompt.contains("N'invente JAMAIS"))
        assertTrue(prompt.contains("nuit synthétique"))
        assertTrue(prompt.contains("1 | A | X | Al | Synth"))
    }
}
