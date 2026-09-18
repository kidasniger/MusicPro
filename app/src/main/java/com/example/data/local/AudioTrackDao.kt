package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioTrackDao {

    @Query("SELECT * FROM audio_tracks ORDER BY title ASC")
    fun getAllTracks(): Flow<List<AudioTrackEntity>>

    @Query("SELECT * FROM audio_tracks WHERE lastPlayed > 0 ORDER BY lastPlayed DESC LIMIT :limit")
    fun getRecentTracks(limit: Int = 20): Flow<List<AudioTrackEntity>>

    @Query("UPDATE audio_tracks SET lastPlayed = :timestamp WHERE id = :trackId")
    suspend fun updateLastPlayed(trackId: Long, timestamp: Long)

    @Query("SELECT * FROM audio_tracks WHERE id = :id LIMIT 1")
    suspend fun getTrackById(id: Long): AudioTrackEntity?

    @Query("""
        SELECT * FROM audio_tracks 
        WHERE title LIKE '%' || :query || '%' 
           OR artist LIKE '%' || :query || '%' 
           OR album LIKE '%' || :query || '%'
        ORDER BY title ASC
    """)
    fun searchTracks(query: String): Flow<List<AudioTrackEntity>>

    @Query("SELECT * FROM audio_tracks WHERE album = :album ORDER BY title ASC")
    fun getTracksByAlbum(album: String): Flow<List<AudioTrackEntity>>

    @Query("SELECT * FROM audio_tracks WHERE artist = :artist ORDER BY title ASC")
    fun getTracksByArtist(artist: String): Flow<List<AudioTrackEntity>>

    @Query("SELECT * FROM audio_tracks WHERE folder = :folder ORDER BY title ASC")
    fun getTracksByFolder(folder: String): Flow<List<AudioTrackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<AudioTrackEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: AudioTrackEntity)

    @Query("DELETE FROM audio_tracks")
    suspend fun clearAllTracks()

    @Query("DELETE FROM audio_tracks WHERE id BETWEEN :startId AND :endId OR contentUri LIKE 'content://media/external/audio/media/100%' OR path LIKE '%demo_track%'")
    suspend fun deleteLegacyDemoTracks(startId: Long = 1000L, endId: Long = 1020L)

    @Query("SELECT COUNT(*) FROM audio_tracks")
    suspend fun getTrackCount(): Int
}
