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
        art: String? = null
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
        lastPlayed = lastPlayed
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
