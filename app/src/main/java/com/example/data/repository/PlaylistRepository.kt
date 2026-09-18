package com.example.data.repository

import com.example.data.local.AudioTrackDao
import com.example.data.local.AudioTrackEntity
import com.example.data.local.PlaylistDao
import com.example.data.local.PlaylistEntity
import com.example.data.local.PlaylistSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class PlaylistRepository(
    private val playlistDao: PlaylistDao,
    private val audioTrackDao: AudioTrackDao
) {

    val allPlaylists: Flow<List<PlaylistEntity>> = playlistDao.getAllPlaylists()

    fun getPlaylistById(playlistId: Long): Flow<PlaylistEntity?> {
        return playlistDao.getPlaylistById(playlistId)
    }

    fun getTracksForPlaylist(playlistId: Long): Flow<List<AudioTrackEntity>> {
        return playlistDao.getTracksForPlaylist(playlistId)
    }

    /**
     * Fournit la liste réactive des playlists enrichies avec les métadonnées (nombre de pistes, durée totale, pochettes).
     */
    fun getPlaylistSummaries(): Flow<List<PlaylistSummary>> {
        return allPlaylists.flatMapLatest { playlists ->
            if (playlists.isEmpty()) {
                kotlinx.coroutines.flow.flowOf(emptyList())
            } else {
                combine(
                    playlists.map { playlist ->
                        playlistDao.getTracksForPlaylist(playlist.id)
                    }
                ) { tracksPerPlaylist ->
                    playlists.mapIndexed { index, playlist ->
                        val tracks = tracksPerPlaylist.getOrNull(index) ?: emptyList()
                        val totalDuration = tracks.sumOf { it.duration }
                        val sampleArts = tracks.mapNotNull { it.albumArtUri }.distinct().take(4)
                        PlaylistSummary(
                            id = playlist.id,
                            name = playlist.name,
                            description = playlist.description,
                            createdAt = playlist.createdAt,
                            updatedAt = playlist.updatedAt,
                            trackCount = tracks.size,
                            totalDurationMs = totalDuration,
                            sampleArtworkUris = sampleArts
                        )
                    }
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun createPlaylist(name: String, description: String = ""): Long = withContext(Dispatchers.IO) {
        val entity = PlaylistEntity(
            name = name.trim(),
            description = description.trim(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        playlistDao.insertPlaylist(entity)
    }

    suspend fun updatePlaylistName(playlistId: Long, newName: String, newDescription: String? = null) = withContext(Dispatchers.IO) {
        val existing = playlistDao.getPlaylistByIdOnce(playlistId) ?: return@withContext
        val updated = existing.copy(
            name = newName.trim(),
            description = newDescription?.trim() ?: existing.description,
            updatedAt = System.currentTimeMillis()
        )
        playlistDao.updatePlaylist(updated)
    }

    suspend fun deletePlaylist(playlistId: Long) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylistById(playlistId)
    }

    suspend fun addTracksToPlaylist(playlistId: Long, trackIds: List<Long>) = withContext(Dispatchers.IO) {
        playlistDao.addTracksToPlaylist(playlistId, trackIds)
    }

    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) = withContext(Dispatchers.IO) {
        playlistDao.removeTrackFromPlaylist(playlistId, trackId)
        playlistDao.updatePlaylistTimestamp(playlistId)
    }

    suspend fun reorderTracks(playlistId: Long, orderedTrackIds: List<Long>) = withContext(Dispatchers.IO) {
        playlistDao.reorderTracks(playlistId, orderedTrackIds)
    }

    suspend fun getTracksOnce(playlistId: Long): List<AudioTrackEntity> = withContext(Dispatchers.IO) {
        playlistDao.getTracksForPlaylistOnce(playlistId)
    }

    companion object {
        @Volatile
        private var INSTANCE: PlaylistRepository? = null

        fun getInstance(context: android.content.Context): PlaylistRepository {
            return INSTANCE ?: synchronized(this) {
                val db = com.example.data.local.MusicProDatabase.getInstance(context)
                val instance = PlaylistRepository(db.playlistDao(), db.audioTrackDao())
                INSTANCE = instance
                instance
            }
        }
    }
}
