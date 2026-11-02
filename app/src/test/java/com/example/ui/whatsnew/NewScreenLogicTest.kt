package com.example.ui.whatsnew

import com.example.data.local.AudioTrackEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Mise à jour 021 : règles de tri et de regroupement de l'écran Nouveau. */
class NewScreenLogicTest {

    private fun track(
        id: Long,
        album: String = "Album",
        added: Long = 0L,
        played: Long = 0L,
        art: String? = null
    ) = AudioTrackEntity(
        id = id,
        title = "Titre $id",
        artist = "Artiste",
        album = album,
        duration = 1000L,
        contentUri = "content://media/$id",
        albumArtUri = art,
        dateAdded = added,
        lastPlayed = played
    )

    @Test
    fun recentTracks_are_newest_first_and_limited() {
        val list = (1L..30L).map { track(it, added = it) }
        val result = recentTracks(list)
        assertEquals(NewCarouselLimit, result.size)
        assertEquals(30L, result.first().id)
    }

    @Test
    fun unplayedTracks_exclude_played_ones() {
        val list = listOf(track(1, played = 5L), track(2, added = 1L), track(3, added = 2L))
        assertEquals(listOf(3L, 2L), unplayedTracks(list).map { it.id })
    }

    @Test
    fun recentAlbums_group_sort_and_ignore_blank_album() {
        val list = listOf(
            track(1, album = "A", added = 1L),
            track(2, album = "A", added = 5L, art = "art://a"),
            track(3, album = "B", added = 3L),
            track(4, album = "", added = 9L)
        )
        val result = recentAlbums(list)
        assertEquals(listOf("A", "B"), result.map { it.name })
        assertEquals("art://a", result[0].coverUri)
        assertEquals(2, result[0].tracks.size)
        assertNull(result[1].coverUri)
    }
}
