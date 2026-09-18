package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Query("SELECT * FROM playlists ORDER BY updatedAt DESC, createdAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :playlistId LIMIT 1")
    fun getPlaylistById(playlistId: Long): Flow<PlaylistEntity?>

    @Query("SELECT * FROM playlists WHERE id = :playlistId LIMIT 1")
    suspend fun getPlaylistByIdOnce(playlistId: Long): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylistById(playlistId: Long)

    @Query("UPDATE playlists SET updatedAt = :timestamp WHERE id = :playlistId")
    suspend fun updatePlaylistTimestamp(playlistId: Long, timestamp: Long = System.currentTimeMillis())

    @Query("""
        SELECT t.* FROM audio_tracks t
        INNER JOIN playlist_track_cross_ref r ON t.id = r.trackId
        WHERE r.playlistId = :playlistId
        ORDER BY r.orderIndex ASC, r.addedAt ASC
    """)
    fun getTracksForPlaylist(playlistId: Long): Flow<List<AudioTrackEntity>>

    @Query("""
        SELECT t.* FROM audio_tracks t
        INNER JOIN playlist_track_cross_ref r ON t.id = r.trackId
        WHERE r.playlistId = :playlistId
        ORDER BY r.orderIndex ASC, r.addedAt ASC
    """)
    suspend fun getTracksForPlaylistOnce(playlistId: Long): List<AudioTrackEntity>

    @Query("SELECT COUNT(*) FROM playlist_track_cross_ref WHERE playlistId = :playlistId")
    fun getTrackCountForPlaylist(playlistId: Long): Flow<Int>

    @Query("SELECT MAX(orderIndex) FROM playlist_track_cross_ref WHERE playlistId = :playlistId")
    suspend fun getMaxOrderIndex(playlistId: Long): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(crossRef: PlaylistTrackCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRefs(crossRefs: List<PlaylistTrackCrossRef>)

    @Query("DELETE FROM playlist_track_cross_ref WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long)

    @Query("DELETE FROM playlist_track_cross_ref WHERE playlistId = :playlistId")
    suspend fun clearPlaylistTracks(playlistId: Long)

    @Query("UPDATE playlist_track_cross_ref SET orderIndex = :newOrderIndex WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun updateOrderIndex(playlistId: Long, trackId: Long, newOrderIndex: Int)

    @Transaction
    suspend fun reorderTracks(playlistId: Long, orderedTrackIds: List<Long>) {
        orderedTrackIds.forEachIndexed { index, trackId ->
            updateOrderIndex(playlistId, trackId, index)
        }
        updatePlaylistTimestamp(playlistId)
    }

    @Transaction
    suspend fun addTracksToPlaylist(playlistId: Long, trackIds: List<Long>) {
        val currentMax = getMaxOrderIndex(playlistId) ?: -1
        var nextOrder = currentMax + 1
        val refs = trackIds.map { trackId ->
            PlaylistTrackCrossRef(
                playlistId = playlistId,
                trackId = trackId,
                orderIndex = nextOrder++,
                addedAt = System.currentTimeMillis()
            )
        }
        insertCrossRefs(refs)
        updatePlaylistTimestamp(playlistId)
    }
}
