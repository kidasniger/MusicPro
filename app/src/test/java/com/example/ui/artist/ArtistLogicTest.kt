package com.example.ui.artist

import com.example.data.local.AudioTrackEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArtistLogicTest {

    private fun track(
        id: Long,
        artist: String,
        album: String = "A",
        title: String = "t$id",
        folder: String = "f",
        lastPlayed: Long = 0L,
        duration: Long = 60_000L,
        art: String? = null,
        dateAdded: Long = 0L
    ) = AudioTrackEntity(
        id = id,
        title = title,
        artist = artist,
        album = album,
        duration = duration,
        contentUri = "content://$id",
        albumArtUri = art,
        path = "/m/$id.mp3",
        folder = folder,
        lastPlayed = lastPlayed,
        dateAdded = dateAdded
    )

    @Test
    fun blankArtistIsGroupedUnderTheUnknownName() {
        val tracks = listOf(track(1, ""), track(2, "Zed"))
        assertEquals(listOf(1L), artistTracksOf(tracks, UnknownArtistName).map { it.id })
        assertEquals(listOf(2L), artistTracksOf(tracks, "Zed").map { it.id })
    }

    @Test
    fun releasesAreSplitBetweenDiscographyAndSingles() {
        val tracks = (1L..4L).map { track(it, "X", album = "Long") } +
            listOf(track(10, "X", album = "Solo"), track(11, "X", album = "Duo"), track(12, "X", album = "Duo"))
        val releases = artistReleasesOf(tracks)
        assertEquals(listOf("Long"), discographyOf(releases).map { it.name })
        assertEquals(listOf("Duo", "Solo"), singlesOf(releases).map { it.name })
    }

    @Test
    fun topTracksAreOrderedByLastPlayedThenTitleAndLimited() {
        val tracks = listOf(
            track(1, "X", title = "b", lastPlayed = 0),
            track(2, "X", title = "a", lastPlayed = 0),
            track(3, "X", title = "z", lastPlayed = 50),
            track(4, "X", title = "y", lastPlayed = 90)
        )
        assertEquals(listOf(4L, 3L, 2L), topTracksOf(tracks, limit = 3).map { it.id })
    }

    @Test
    fun `mise à jour 066 — les plus joués dominent puis dernière écoute`() {
        val tracks = listOf(
            track(1, "X", title = "peu joué", lastPlayed = 100),
            track(2, "X", title = "très joué", lastPlayed = 0),
            track(3, "X", title = "moyen", lastPlayed = 50)
        )
        val counts = mapOf(2L to 12, 3L to 3, 1L to 1)
        assertEquals(listOf(2L, 3L, 1L), topTracksOf(tracks, limit = 3, playCounts = counts).map { it.id })
    }

    @Test
    fun `mise à jour 066 — sans historique l'ordre retombe sur la dernière écoute`() {
        val tracks = listOf(track(1, "X", title = "b", lastPlayed = 5), track(2, "X", title = "a", lastPlayed = 9))
        assertEquals(listOf(2L, 1L), topTracksOf(tracks, limit = 2).map { it.id })
    }

    @Test
    fun `mise à jour 067 — apparitions seulement hors albums dominés par l'artiste`() {
        val all = listOf(
            // Album « Coop » : 2 titres de X, 3 de Y → apparition pour X, album de Y.
            track(1, "X", album = "Coop"), track(2, "X", album = "Coop"),
            track(3, "Y", album = "Coop"), track(4, "Y", album = "Coop"), track(5, "Y", album = "Coop"),
            // Album « Solo X » : dominé par X → pas une apparition.
            track(6, "X", album = "Solo X"), track(7, "X", album = "Solo X"),
            // Album « Autre » : aucun titre de X → ignoré.
            track(8, "Y", album = "Autre")
        )
        val appearances = appearancesOf(all, "X")
        assertEquals(listOf("Coop"), appearances.map { it.name })
        assertEquals(listOf(1L, 2L), appearances.first().tracks.map { it.id })
    }

    @Test
    fun `mise à jour 067 — année déduite de la date d'ajout`() {
        assertEquals(2024, releaseYearOf(1706745600L)) // 1er février 2024 UTC
        assertEquals(null, releaseYearOf(0L))
        val release = artistReleasesOf(listOf(track(1, "X", album = "A", dateAdded = 1706745600L)))
        assertEquals(2024, release.first().year)
    }

    @Test
    fun durationIsRoundedToTheMinuteWithOneMinuteMinimum() {
        assertEquals(0, artistDurationMinutes(emptyList()))
        assertEquals(1, artistDurationMinutes(listOf(track(1, "X", duration = 5_000L))))
        assertEquals(3, artistDurationMinutes(listOf(track(1, "X", duration = 60_000L), track(2, "X", duration = 120_000L))))
    }

    @Test
    fun similarArtistsShareFoldersAndExcludeTheArtist() {
        val tracks = listOf(
            track(1, "Me", folder = "rock"),
            track(2, "B", folder = "rock", art = "content://art"),
            track(3, "B", folder = "rock"),
            track(4, "C", folder = "rock"),
            track(5, "D", folder = "jazz")
        )
        val similar = similarArtistsOf(tracks, "Me")
        assertEquals(listOf("B", "C"), similar.map { it.name })
        assertEquals("content://art", similar.first().coverUri)
        assertTrue(similarArtistsOf(tracks, "Nobody").isEmpty())
    }

    @Test
    fun nameFontSizeShrinksForLongNames() {
        assertEquals(80, artistNameFontSizeSp("Daft"))
        assertEquals(64, artistNameFontSizeSp("Coldplay X1"))
        assertEquals(48, artistNameFontSizeSp("Red Hot Chili Peppers"))
    }
}
