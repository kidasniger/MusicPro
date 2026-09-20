
package com.example.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AudioTrackEntity
import com.example.data.local.MusicProDatabase
import com.example.data.scanner.AudioScanner
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AudioRepositoryTest {

    private lateinit var database: MusicProDatabase
    private lateinit var scanner: FakeAudioScanner
    private lateinit var audioRepository: AudioRepository
    private lateinit var playlistRepository: PlaylistRepository

    private val originalTrack = AudioTrackEntity(
        id = 401L,
        title = "Original",
        artist = "Artist",
        album = "Album",
        duration = 180_000L,
        contentUri = "content://media/external/audio/media/401",
        path = "/storage/emulated/0/Music/original.mp3",
        folder = "Music",
        mimeType = "audio/mpeg",
        size = 1_024L
    )

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, MusicProDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        scanner = FakeAudioScanner()
        audioRepository = AudioRepository(database.audioTrackDao(), scanner)
        playlistRepository = PlaylistRepository(database.playlistDao(), database.audioTrackDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun rescanUpdatesTrackWithoutRemovingPlaylistRelation() = runTest {
        database.audioTrackDao().insertTrack(originalTrack)
        val playlistId = playlistRepository.createPlaylist("Safe Rescan")
        playlistRepository.addTracksToPlaylist(playlistId, listOf(originalTrack.id))

        scanner.tracks = listOf(originalTrack.copy(title = "Updated"))

        val count = audioRepository.refreshMediaStoreScan()

        assertEquals(1, count)
        val playlistTracks = playlistRepository.getTracksForPlaylist(playlistId).first()
        assertEquals(1, playlistTracks.size)
        assertEquals(originalTrack.id, playlistTracks[0].id)
        assertEquals("Updated", playlistTracks[0].title)
    }

    @Test
    fun emptyRescanDoesNotClearTracksOrPlaylistRelation() = runTest {
        database.audioTrackDao().insertTrack(originalTrack)
        val playlistId = playlistRepository.createPlaylist("Empty Scan Safe")
        playlistRepository.addTracksToPlaylist(playlistId, listOf(originalTrack.id))

        scanner.tracks = emptyList()

        val count = audioRepository.refreshMediaStoreScan()

        assertEquals(1, count)
        assertEquals(1, database.audioTrackDao().getTrackCount())
        assertEquals(1, playlistRepository.getTracksForPlaylist(playlistId).first().size)
        assertTrue(database.audioTrackDao().getTrackById(originalTrack.id) != null)
    }

    private class FakeAudioScanner : AudioScanner {
        var tracks: List<AudioTrackEntity> = emptyList()

        override suspend fun scanAudioFiles(): List<AudioTrackEntity> = tracks
    }
}
