
package com.example.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MusicProDatabaseMigrationTest {

    private val dbName = "musicpro_migration_test"
    private lateinit var context: Context
    private var migratedDatabase: MusicProDatabase? = null

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(dbName)
    }

    @After
    fun tearDown() {
        migratedDatabase?.close()
        context.deleteDatabase(dbName)
    }

    @Test
    fun migration1To3PreservesExistingAudioTracks() {
        createVersionOneDatabase()

        migratedDatabase = Room.databaseBuilder(
            context,
            MusicProDatabase::class.java,
            dbName
        )
            .allowMainThreadQueries()
            .addMigrations(
                MusicProDatabase.MIGRATION_1_2,
                MusicProDatabase.MIGRATION_2_3
            )
            .build()

        val tracks = runBlocking {
            migratedDatabase!!.audioTrackDao().getAllTracksSnapshot()
        }

        assertEquals(1, tracks.size)
        assertEquals(501L, tracks.single().id)
        assertEquals("Legacy Track", tracks.single().title)
        assertEquals(0L, tracks.single().lastPlayed)
    }

    @Test
    fun migration2To3PreservesPlaylistRelations() {
        createVersionTwoDatabase()

        migratedDatabase = Room.databaseBuilder(
            context,
            MusicProDatabase::class.java,
            dbName
        )
            .allowMainThreadQueries()
            .addMigrations(
                MusicProDatabase.MIGRATION_1_2,
                MusicProDatabase.MIGRATION_2_3
            )
            .build()

        val tracks = runBlocking {
            migratedDatabase!!.playlistDao().getTracksForPlaylistOnce(600L)
        }

        assertEquals(1, tracks.size)
        assertEquals(601L, tracks.single().id)
        assertEquals("Playlist Legacy Track", tracks.single().title)
        assertEquals(0L, tracks.single().lastPlayed)
    }

    private fun createVersionOneDatabase() {
        val db = context.openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null)
        db.execSQL(
            """
            CREATE TABLE audio_tracks (
                id INTEGER NOT NULL,
                title TEXT NOT NULL,
                artist TEXT NOT NULL,
                album TEXT NOT NULL,
                duration INTEGER NOT NULL,
                contentUri TEXT NOT NULL,
                albumArtUri TEXT,
                path TEXT NOT NULL,
                folder TEXT NOT NULL,
                mimeType TEXT,
                size INTEGER NOT NULL,
                hasSyncedLyrics INTEGER NOT NULL,
                dateAdded INTEGER NOT NULL,
                PRIMARY KEY(id)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO audio_tracks(
                id, title, artist, album, duration, contentUri, albumArtUri,
                path, folder, mimeType, size, hasSyncedLyrics, dateAdded
            ) VALUES (
                501, 'Legacy Track', 'Legacy Artist', 'Legacy Album', 120000,
                'content://media/501', NULL, '/music/legacy.mp3', 'Music',
                'audio/mpeg', 1234, 0, 100
            )
            """.trimIndent()
        )
        db.version = 1
        db.close()
    }

    private fun createVersionTwoDatabase() {
        createVersionOneDatabase()

        val db = context.openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null)
        db.execSQL(
            """
            CREATE TABLE playlists (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                description TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE playlist_track_cross_ref (
                playlistId INTEGER NOT NULL,
                trackId INTEGER NOT NULL,
                orderIndex INTEGER NOT NULL,
                addedAt INTEGER NOT NULL,
                PRIMARY KEY(playlistId, trackId),
                FOREIGN KEY(playlistId) REFERENCES playlists(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(trackId) REFERENCES audio_tracks(id) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX index_playlist_track_cross_ref_playlistId ON playlist_track_cross_ref(playlistId)")
        db.execSQL("CREATE INDEX index_playlist_track_cross_ref_trackId ON playlist_track_cross_ref(trackId)")
        db.execSQL(
            "CREATE INDEX index_playlist_track_cross_ref_playlistId_orderIndex " +
                "ON playlist_track_cross_ref(playlistId, orderIndex)"
        )
        db.execSQL(
            "INSERT INTO playlists(id, name, description, createdAt, updatedAt) " +
                "VALUES(600, 'Legacy Playlist', 'Kept', 100, 100)"
        )
        db.execSQL(
            "INSERT INTO audio_tracks(id, title, artist, album, duration, contentUri, albumArtUri, path, folder, mimeType, size, hasSyncedLyrics, dateAdded) " +
                "VALUES(601, 'Playlist Legacy Track', 'Artist', 'Album', 180000, 'content://media/601', NULL, '/music/playlist.mp3', 'Music', 'audio/mpeg', 2222, 0, 200)"
        )
        db.execSQL(
            "INSERT INTO playlist_track_cross_ref(playlistId, trackId, orderIndex, addedAt) " +
                "VALUES(600, 601, 0, 200)"
        )
        db.version = 2
        db.close()
    }
}
