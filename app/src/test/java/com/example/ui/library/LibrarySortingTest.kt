package com.example.ui.library

import com.example.data.local.AudioTrackEntity
import com.example.data.preferences.LibrarySortMode
import com.example.ui.audio.AlbumSummary
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Mise à jour 055 — tri de la bibliothèque (A–Z, Récents, Durée, Plus écoutés).
 */
class LibrarySortingTest {

    private fun track(
        id: Long,
        title: String,
        duration: Long = 180_000L,
        dateAdded: Long = 0L,
        lastPlayed: Long = 0L
    ) = AudioTrackEntity(
        id = id,
        title = title,
        artist = "Artiste",
        album = "Album",
        duration = duration,
        contentUri = "content://media/audio/$id",
        dateAdded = dateAdded,
        lastPlayed = lastPlayed
    )

    @Test
    fun `A–Z trie par titre insensible à la casse`() {
        val sorted = LibrarySorting.sortTracks(
            listOf(track(1, "zèbre"), track(2, "Alpha"), track(3, "Midi")),
            LibrarySortMode.TITLE_AZ
        )
        assertEquals(listOf("Alpha", "Midi", "zèbre"), sorted.map { it.title })
    }

    @Test
    fun `Récents trie par date d'ajout décroissante`() {
        val sorted = LibrarySorting.sortTracks(
            listOf(
                track(1, "Ancien", dateAdded = 100L),
                track(2, "Récent", dateAdded = 900L),
                track(3, "Milieu", dateAdded = 500L)
            ),
            LibrarySortMode.RECENT
        )
        assertEquals(listOf(2L, 3L, 1L), sorted.map { it.id })
    }

    @Test
    fun `Durée trie par durée décroissante`() {
        val sorted = LibrarySorting.sortTracks(
            listOf(
                track(1, "Court", duration = 60_000L),
                track(2, "Long", duration = 400_000L),
                track(3, "Moyen", duration = 200_000L)
            ),
            LibrarySortMode.DURATION
        )
        assertEquals(listOf(2L, 3L, 1L), sorted.map { it.id })
    }

    @Test
    fun `Plus écoutés trie par dernière lecture`() {
        val sorted = LibrarySorting.sortTracks(
            listOf(
                track(1, "Jamais joué"),
                track(2, "Hier", lastPlayed = 1_000L),
                track(3, "Aujourd'hui", lastPlayed = 5_000L)
            ),
            LibrarySortMode.MOST_PLAYED
        )
        assertEquals(listOf(3L, 2L, 1L), sorted.map { it.id })
    }

    @Test
    fun `les albums retombent sur l'ordre alphabétique pour le tri Durée`() {
        val sample = track(1, "Titre")
        val albums = listOf(
            AlbumSummary("Zeta", "A", 2, null, sample),
            AlbumSummary("alpha", "B", 1, null, sample)
        )
        val sorted = LibrarySorting.sortAlbums(albums, LibrarySortMode.DURATION)
        assertEquals(listOf("alpha", "Zeta"), sorted.map { it.name })
    }
}
