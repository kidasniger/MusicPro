package com.example.data.repository

import android.content.Context
import com.example.data.local.AudioTrackDao
import com.example.data.local.AudioTrackEntity
import com.example.data.local.MusicProDatabase
import com.example.data.scanner.MediaStoreAudioScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AudioRepository(
    private val audioTrackDao: AudioTrackDao,
    private val scanner: MediaStoreAudioScanner
) {

    val allTracks: Flow<List<AudioTrackEntity>> = audioTrackDao.getAllTracks()

    fun getRecentTracks(limit: Int = 20): Flow<List<AudioTrackEntity>> {
        return audioTrackDao.getRecentTracks(limit)
    }

    suspend fun updateLastPlayed(trackId: Long, timestamp: Long = System.currentTimeMillis()) = withContext(Dispatchers.IO) {
        audioTrackDao.updateLastPlayed(trackId, timestamp)
    }

    fun searchTracks(query: String): Flow<List<AudioTrackEntity>> {
        return audioTrackDao.searchTracks(query.trim())
    }

    fun getTracksByAlbum(album: String): Flow<List<AudioTrackEntity>> {
        return audioTrackDao.getTracksByAlbum(album)
    }

    fun getTracksByArtist(artist: String): Flow<List<AudioTrackEntity>> {
        return audioTrackDao.getTracksByArtist(artist)
    }

    fun getTracksByFolder(folder: String): Flow<List<AudioTrackEntity>> {
        return audioTrackDao.getTracksByFolder(folder)
    }

    suspend fun getTrackCount(): Int = withContext(Dispatchers.IO) {
        audioTrackDao.getTrackCount()
    }

    /**
     * Rescanne le MediaStore et met à jour le cache Room local.
     * Les fichiers supprimés du téléphone sont automatiquement retirés de la base de données.
     * Préserve le statut hasSyncedLyrics des morceaux déjà indexés.
     */
    suspend fun refreshMediaStoreScan(): Int = withContext(Dispatchers.IO) {
        val existingTracks = try {
            audioTrackDao.getAllTracksSnapshot().associateBy { it.id }
        } catch (e: Exception) {
            emptyMap()
        }
        val scannedTracks = scanner.scanAudioFiles()
        val mergedTracks = scannedTracks.map { scanned ->
            val prev = existingTracks[scanned.id]
            if (prev != null && prev.hasSyncedLyrics) {
                scanned.copy(hasSyncedLyrics = true)
            } else {
                scanned
            }
        }
        audioTrackDao.clearAllTracks()
        if (mergedTracks.isNotEmpty()) {
            audioTrackDao.insertTracks(mergedTracks)
        }
        mergedTracks.size
    }

    suspend fun updateLyricsStatus(trackId: Long, hasLyrics: Boolean) = withContext(Dispatchers.IO) {
        try {
            audioTrackDao.updateLyricsStatus(trackId, hasLyrics)
        } catch (e: Exception) {
            android.util.Log.e("AudioRepository", "Erreur updateLyricsStatus: ${e.message}")
        }
    }

    /**
     * Purge définitivement toutes les anciennes pistes de démonstration ou fictives
     * présentes dans la base de données Room locale.
     */
    suspend fun purgeLegacyDemoTracks() = withContext(Dispatchers.IO) {
        try {
            audioTrackDao.deleteLegacyDemoTracks()
        } catch (e: Exception) {
            android.util.Log.e("AudioRepository", "Erreur lors de la purge des pistes de démo: ${e.message}")
        }
    }

    suspend fun clearCache() = withContext(Dispatchers.IO) {
        audioTrackDao.clearAllTracks()
    }

    companion object {
        @Volatile
        private var INSTANCE: AudioRepository? = null

        fun getInstance(context: Context): AudioRepository {
            return INSTANCE ?: synchronized(this) {
                val db = MusicProDatabase.getInstance(context)
                val scanner = MediaStoreAudioScanner(context)
                val instance = AudioRepository(db.audioTrackDao(), scanner)
                INSTANCE = instance
                instance
            }
        }
    }
}
