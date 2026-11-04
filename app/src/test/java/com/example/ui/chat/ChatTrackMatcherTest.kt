package com.example.ui.chat

import com.example.data.local.AudioTrackEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Mise à jour 096 — rapprochement entre les morceaux cités dans une réponse
 * et la bibliothèque Room locale : si le morceau n'existe pas localement,
 * il reste en texte simple (aucune carte).
 */
class ChatTrackMatcherTest {

    private fun track(id: Long, title: String, artist: String = "Artiste") = AudioTrackEntity(
        id = id,
        title = title,
        artist = artist,
        album = "Album",
        duration = 180_000L,
        contentUri = "content://media/$id"
    )

    @Test
    fun `titre cité tel quel est retrouvé`() {
        val library = listOf(track(1, "Salina"), track(2, "An ba nou"))
        val found = findMentionedTracks("Écoute « Salina » de HC Turbionaire.", library)
        assertEquals(listOf(1L), found.map { it.id })
    }

    @Test
    fun `titre absent de la bibliothèque reste en texte simple`() {
        val library = listOf(track(1, "Salina"))
        val found = findMentionedTracks("Je conseille Zouk La Seél Slo, un classique.", library)
        assertTrue(found.isEmpty())
    }

    @Test
    fun `casse et ponctuation ne bloquent pas le rapprochement`() {
        val library = listOf(track(1, "Métamorphose"))
        val found = findMentionedTracks("avec MÉTAMORPHOSE!!!", library)
        assertEquals(listOf(1L), found.map { it.id })
    }

    @Test
    fun `le titre le plus long gagne`() {
        val library = listOf(track(1, "Love Song"), track(2, "Love Song (Live)"))
        val found = findMentionedTracks("Play Love Song (Live)", library)
        assertEquals(listOf(2L, 1L), found.map { it.id })
    }

    @Test
    fun `maximum six cartes et pas de doublon`() {
        val library = (1L..10L).map { track(it, "Morceau $it") }
        val found = findMentionedTracks("Morceau 1, Morceau 1, Morceau 2, Morceau 3, Morceau 4, Morceau 5, Morceau 6, Morceau 7", library)
        assertEquals(6, found.size)
        assertEquals(6, found.map { it.id }.distinct().size)
    }

    @Test
    fun `texte vide ne renvoie rien`() {
        val library = listOf(track(1, "Salina"))
        assertTrue(findMentionedTracks("", library).isEmpty())
        assertTrue(findMentionedTracks("Salina", emptyList()).isEmpty())
    }
}
