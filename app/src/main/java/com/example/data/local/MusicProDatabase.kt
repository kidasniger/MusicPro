package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        AudioTrackEntity::class,
        PlaylistEntity::class,
        PlaylistTrackCrossRef::class,
        ListeningStatEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class MusicProDatabase : RoomDatabase() {

    abstract fun audioTrackDao(): AudioTrackDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun listeningStatsDao(): ListeningStatsDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS playlists (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        description TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS playlist_track_cross_ref (
                        playlistId INTEGER NOT NULL,
                        trackId INTEGER NOT NULL,
                        orderIndex INTEGER NOT NULL,
                        addedAt INTEGER NOT NULL,
                        PRIMARY KEY(playlistId, trackId),
                        FOREIGN KEY(playlistId) REFERENCES playlists(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(trackId) REFERENCES audio_tracks(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_playlist_track_cross_ref_playlistId ON playlist_track_cross_ref(playlistId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_playlist_track_cross_ref_trackId ON playlist_track_cross_ref(trackId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_playlist_track_cross_ref_playlistId_orderIndex ON playlist_track_cross_ref(playlistId, orderIndex)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE audio_tracks ADD COLUMN lastPlayed INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE audio_tracks ADD COLUMN genre TEXT NOT NULL DEFAULT ''")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS listening_stats (
                        dayEpoch INTEGER NOT NULL,
                        trackId INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        artist TEXT NOT NULL,
                        genre TEXT NOT NULL,
                        listenedMs INTEGER NOT NULL,
                        PRIMARY KEY(dayEpoch, trackId)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_listening_stats_dayEpoch ON listening_stats(dayEpoch)")
            }
        }

        /**
         * Retrait du chat IA : les tables des conversations (mises à jour 097) sont
         * supprimées. MIGRATION_4_6 couvre les installations restées en version 4,
         * MIGRATION_5_6 celles qui avaient déjà reçu les tables du chat.
         */
        val MIGRATION_4_6 = object : Migration(4, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS chat_messages")
                db.execSQL("DROP TABLE IF EXISTS chat_conversations")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS chat_messages")
                db.execSQL("DROP TABLE IF EXISTS chat_conversations")
            }
        }

        private val ALL_MIGRATIONS = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_6, MIGRATION_5_6)

        @Volatile
        private var INSTANCE: MusicProDatabase? = null

        fun getInstance(context: Context): MusicProDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MusicProDatabase::class.java,
                    "musicpro_database"
                )
                    .addMigrations(*ALL_MIGRATIONS)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
