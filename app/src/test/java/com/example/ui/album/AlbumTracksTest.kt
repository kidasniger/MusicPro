package com.example.ui.album

import com.example.data.local.AudioTrackEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class AlbumTracksTest {

    private fun track(id: Long, album: String, path: String, duration: Long = 60_000L) = AudioTrackEntity(
        id = id,
        title = "t$id",
        artist = "a",
        album = album,
        duration = duration,
        contentUri = "content://$id",
        path = path
    )

    @Test
    fun keepsOnlyTheAlbumAndSortsByFilePath() {
        val tracks = listOf(
            track(1, "A", "/m/02 b.mp3"),
            track(2, "B", "/m/01 x.mp3"),
            track(3, "A", "/m/01 a.mp3")
        )
        assertEquals(listOf(3L, 1L), albumTracksOf(tracks, "A").map { it.id })
    }

    @Test
    fun durationIsRoundedToTheMinuteWithOneMinuteMinimum() {
        assertEquals(0, albumDurationMinutes(emptyList()))
        assertEquals(1, albumDurationMinutes(listOf(track(1, "A", "p", duration = 10_000L))))
        assertEquals(3, albumDurationMinutes(listOf(track(1, "A", "p", 60_000L), track(2, "A", "q", 120_000L))))
    }
}
