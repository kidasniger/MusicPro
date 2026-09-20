package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
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

    @Upsert
    suspend fun upsertTracks(tracks: List<AudioTrackEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: AudioTrackEntity)

    @Query("DELETE FROM audio_tracks")
    suspend fun clearAllTracks()

    @Query("DELETE FROM audio_tracks WHERE id NOT IN (:ids)")
    suspend fun deleteTracksNotIn(ids: List<Long>)

    @Query("SELECT * FROM audio_tracks")
    suspend fun getAllTracksSnapshot(): List<AudioTrackEntity>

    @Query("UPDATE audio_tracks SET hasSyncedLyrics = :hasLyrics WHERE id = :id")
    suspend fun updateLyricsStatus(id: Long, hasLyrics: Boolean)

    @Query("SELECT COUNT(*) FROM audio_tracks")
    suspend fun getTrackCount(): Int
}
