
package com.example.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
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
                MusicProDatabase.MIGRATION_2_3,
                MusicProDatabase.MIGRATION_3_4,
                MusicProDatabase.MIGRATION_4_6,
                MusicProDatabase.MIGRATION_5_6
            )
            .build()

        val tracks = runBlocking {
            migratedDatabase!!.audioTrackDao().getAllTracksSnapshot()
        }

        assertEquals(1, tracks.size)
        assertEquals(501L, tracks.single().id)
        assertEquals("Legacy Track", tracks.single().title)
        assertEquals(0L, tracks.single().lastPlayed)
        assertEquals("", tracks.single().genre)
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
                MusicProDatabase.MIGRATION_2_3,
                MusicProDatabase.MIGRATION_3_4,
                MusicProDatabase.MIGRATION_4_6,
                MusicProDatabase.MIGRATION_5_6
            )
            .build()

        val tracks = runBlocking {
            migratedDatabase!!.playlistDao().getTracksForPlaylistOnce(600L)
        }

        assertEquals(1, tracks.size)
        assertEquals(601L, tracks.single().id)
        assertEquals("Playlist Legacy Track", tracks.single().title)
        assertEquals(0L, tracks.single().lastPlayed)
        assertEquals("", tracks.single().genre)
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

    /** Passe une base v2 au schéma v4 (colonnes lastPlayed/genre + listening_stats). */
    private fun upgradeVersionTwoToVersionFour() {
        val db = context.openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null)
        db.execSQL("ALTER TABLE audio_tracks ADD COLUMN lastPlayed INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE audio_tracks ADD COLUMN genre TEXT NOT NULL DEFAULT ''")
        db.execSQL(
            "CREATE TABLE listening_stats (dayEpoch INTEGER NOT NULL, trackId INTEGER NOT NULL, " +
                "title TEXT NOT NULL, artist TEXT NOT NULL, genre TEXT NOT NULL, listenedMs INTEGER NOT NULL, " +
                "PRIMARY KEY(dayEpoch, trackId))"
        )
        db.execSQL("CREATE INDEX index_listening_stats_dayEpoch ON listening_stats(dayEpoch)")
    }

    /** Recrée les tables du chat telles qu'elles existaient en version 5. */
    private fun createLegacyChatTables(db: android.database.sqlite.SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE chat_conversations (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "title TEXT NOT NULL, model TEXT NOT NULL, tone TEXT NOT NULL, suggestionKey TEXT NOT NULL, " +
                "createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE chat_messages (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "conversationId INTEGER NOT NULL, role TEXT NOT NULL, content TEXT NOT NULL, " +
                "feedback INTEGER NOT NULL, createdAt INTEGER NOT NULL)"
        )
        db.execSQL("CREATE INDEX index_chat_messages_conversationId ON chat_messages(conversationId)")
        db.execSQL("INSERT INTO chat_conversations(id, title, model, tone, suggestionKey, createdAt, updatedAt) VALUES(1, 'Ancienne conversation', 'fast', 'neutral', '', 100, 100)")
        db.execSQL("INSERT INTO chat_messages(id, conversationId, role, content, feedback, createdAt) VALUES(1, 1, 'user', 'Bonjour', 0, 101)")
    }

    private fun chatTableCount(): Int {
        val cursor = migratedDatabase!!.openHelper.writableDatabase
            .query("SELECT name FROM sqlite_master WHERE type='table' AND name LIKE 'chat_%'")
        cursor.use { return it.count }
    }

    /**
     * Retrait du chat IA — une base v5 qui contient encore les tables du chat est
     * migrée en v6 : les tables sont supprimées, les données musicales intactes.
     */
    @Test
    fun migration5To6DropsChatTables() {
        createVersionTwoDatabase()
        upgradeVersionTwoToVersionFour()
        val db = context.openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null)
        createLegacyChatTables(db)
        db.version = 5
        db.close()

        migratedDatabase = Room.databaseBuilder(
            context,
            MusicProDatabase::class.java,
            dbName
        )
            .allowMainThreadQueries()
            .addMigrations(MusicProDatabase.MIGRATION_5_6)
            .build()

        assertEquals(0, chatTableCount())
        val tracks = runBlocking { migratedDatabase!!.audioTrackDao().getAllTracksSnapshot() }
        assertTrue(tracks.map { it.id }.containsAll(listOf(501L, 601L)))
    }

    /**
     * Retrait du chat IA — une base v4 monte directement en v6 : la migration
     * tolère l'absence des tables du chat et préserve les données musicales.
     */
    @Test
    fun migration4To6UpgradesWithoutChatTables() {
        createVersionTwoDatabase()
        upgradeVersionTwoToVersionFour()
        val db = context.openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null)
        db.version = 4
        db.close()

        migratedDatabase = Room.databaseBuilder(
            context,
            MusicProDatabase::class.java,
            dbName
        )
            .allowMainThreadQueries()
            .addMigrations(MusicProDatabase.MIGRATION_4_6)
            .build()

        assertEquals(0, chatTableCount())
        val tracks = runBlocking { migratedDatabase!!.audioTrackDao().getAllTracksSnapshot() }
        assertTrue(tracks.map { it.id }.containsAll(listOf(501L, 601L)))
    }
}
