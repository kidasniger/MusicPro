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
     */
    suspend fun refreshMediaStoreScan(): Int = withContext(Dispatchers.IO) {
        val scannedTracks = scanner.scanAudioFiles()
        if (scannedTracks.isNotEmpty()) {
            audioTrackDao.clearAllTracks()
            audioTrackDao.insertTracks(scannedTracks)
        }
        scannedTracks.size
    }

    /**
     * Charge les pistes d'exemple haute définition dans Room
     * pour émulateur ou appareil sans fichier local.
     */
    suspend fun loadDemoTracks(): Int = withContext(Dispatchers.IO) {
        val demoTracks = scanner.getFallbackDemoTracks()
        audioTrackDao.clearAllTracks()
        audioTrackDao.insertTracks(demoTracks)
        demoTracks.size
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
