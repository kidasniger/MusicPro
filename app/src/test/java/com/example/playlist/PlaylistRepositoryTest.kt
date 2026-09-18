package com.example.playlist

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AudioTrackEntity
import com.example.data.local.MusicProDatabase
import com.example.data.local.PlaylistSummary
import com.example.data.repository.PlaylistRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PlaylistRepositoryTest {

    private lateinit var database: MusicProDatabase
    private lateinit var repository: PlaylistRepository

    private val sampleTrack1 = AudioTrackEntity(
        id = 101L,
        title = "Neon Nights",
        artist = "SynthWave Band",
        album = "Cyberpunk 2099",
        duration = 180000L, // 3 min
        contentUri = "content://media/audio/101",
        albumArtUri = "content://media/album/101"
    )

    private val sampleTrack2 = AudioTrackEntity(
        id = 102L,
        title = "Midnight Drive",
        artist = "Electro Girl",
        album = "Night City",
        duration = 240000L, // 4 min
        contentUri = "content://media/audio/102",
        albumArtUri = "content://media/album/102"
    )

    private val sampleTrack3 = AudioTrackEntity(
        id = 103L,
        title = "Sunrise Chill",
        artist = "LoFi Dreamer",
        album = "Morning Glow",
        duration = 120000L, // 2 min
        contentUri = "content://media/audio/103",
        albumArtUri = "content://media/album/103"
    )

    @Before
    fun setup() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, MusicProDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = PlaylistRepository(database.playlistDao(), database.audioTrackDao())

        // Insertion des morceaux sources dans la table des morceaux
        database.audioTrackDao().insertTracks(listOf(sampleTrack1, sampleTrack2, sampleTrack3))
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `create playlist and fetch from database`() = runTest {
        val playlistId = repository.createPlaylist("Synthwave Favorites", "Best electronic hits")
        assertTrue(playlistId > 0)

        val fetched = repository.getPlaylistById(playlistId).first()
        assertNotNull(fetched)
        assertEquals("Synthwave Favorites", fetched?.name)
        assertEquals("Best electronic hits", fetched?.description)
    }

    @Test
    fun `add tracks to playlist and verify order and summary`() = runTest {
        val playlistId = repository.createPlaylist("Workout Mix")
        repository.addTracksToPlaylist(playlistId, listOf(101L, 102L, 103L))

        val tracks = repository.getTracksForPlaylist(playlistId).first()
        assertEquals(3, tracks.size)
        assertEquals(101L, tracks[0].id)
        assertEquals(102L, tracks[1].id)
        assertEquals(103L, tracks[2].id)

        val summaries = repository.getPlaylistSummaries().first()
        val summary = summaries.find { it.id == playlistId }
        assertNotNull(summary)
        assertEquals(3, summary?.trackCount)
        assertEquals(540000L, summary?.totalDurationMs) // 180 + 240 + 120 = 540 sec = 9 min
        assertEquals("9:00", summary?.formatDuration())
    }

    @Test
    fun `reorder tracks in playlist simulates drag and drop persistence`() = runTest {
        val playlistId = repository.createPlaylist("Favorites")
        repository.addTracksToPlaylist(playlistId, listOf(101L, 102L, 103L))

        // Initial check
        val initialTracks = repository.getTracksForPlaylist(playlistId).first()
        assertEquals(101L, initialTracks[0].id)
        assertEquals(102L, initialTracks[1].id)
        assertEquals(103L, initialTracks[2].id)

        // Simulate drag & drop: user moves track 103 to position 0, then 101, then 102
        val newOrder = listOf(103L, 101L, 102L)
        repository.reorderTracks(playlistId, newOrder)

        val reorderedTracks = repository.getTracksForPlaylist(playlistId).first()
        assertEquals(3, reorderedTracks.size)
        assertEquals(103L, reorderedTracks[0].id)
        assertEquals(101L, reorderedTracks[1].id)
        assertEquals(102L, reorderedTracks[2].id)
    }

    @Test
    fun `remove track from playlist removes cross ref without deleting base track`() = runTest {
        val playlistId = repository.createPlaylist("Road Trip")
        repository.addTracksToPlaylist(playlistId, listOf(101L, 102L))

        // Remove track 101
        repository.removeTrackFromPlaylist(playlistId, 101L)

        val tracksInPlaylist = repository.getTracksForPlaylist(playlistId).first()
        assertEquals(1, tracksInPlaylist.size)
        assertEquals(102L, tracksInPlaylist[0].id)

        // Base track still exists in audio library
        val baseTrack = database.audioTrackDao().getTrackById(101L)
        assertNotNull(baseTrack)
    }

    @Test
    fun `delete playlist cascades to cross refs but keeps tracks`() = runTest {
        val playlistId = repository.createPlaylist("To Delete")
        repository.addTracksToPlaylist(playlistId, listOf(101L, 102L, 103L))

        repository.deletePlaylist(playlistId)

        val fetched = repository.getPlaylistById(playlistId).first()
        assertNull(fetched)

        // Verify base tracks still intact
        val allTracks = database.audioTrackDao().getAllTracks().first()
        assertEquals(3, allTracks.size)
    }

    @Test
    fun `playlist summary format duration displays hours when exceeding 60 minutes`() {
        val summary1 = PlaylistSummary(id = 1, name = "Short", totalDurationMs = 125000L)
        assertEquals("2:05", summary1.formatDuration())

        val summary2 = PlaylistSummary(id = 2, name = "Long", totalDurationMs = 3665000L) // 1h 1min 5s
        assertEquals("1h 01min", summary2.formatDuration())
    }
}
