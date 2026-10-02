package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.local.AudioTrackDao
import com.example.data.local.AudioTrackEntity
import com.example.data.local.MusicProDatabase
import com.example.data.scanner.AudioScanner
import com.example.data.scanner.MediaStoreAudioScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

class AudioRepository(
    private val audioTrackDao: AudioTrackDao,
    private val scanner: AudioScanner,
    private val context: Context
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

    suspend fun getTrackById(id: Long): AudioTrackEntity? = withContext(Dispatchers.IO) {
        audioTrackDao.getTrackById(id)
    }

    suspend fun getAllTracksSnapshot(): List<AudioTrackEntity> = withContext(Dispatchers.IO) {
        audioTrackDao.getAllTracksSnapshot()
    }

    suspend fun pruneUnavailableTracks(): Int = withContext(Dispatchers.IO) {
        val tracks = audioTrackDao.getAllTracksSnapshot()
        val unavailableIds = tracks
            .filterNot { isTrackAccessible(it) }
            .map { it.id }

        if (unavailableIds.isNotEmpty()) {
            audioTrackDao.deleteTracksByIds(unavailableIds)
        }
        unavailableIds.size
    }

    private fun isTrackAccessible(track: AudioTrackEntity): Boolean {
        val path = track.path.trim()

        if (path.startsWith("http://", ignoreCase = true) ||
            path.startsWith("https://", ignoreCase = true)
        ) {
            return true
        }

        if (path.isNotBlank()) {
            val file = File(path)
            if (file.isFile && file.canRead()) return true
        }

        val uriText = track.contentUri.trim()
        if (uriText.isNotBlank()) {
            return try {
                context.contentResolver
                    .openAssetFileDescriptor(Uri.parse(uriText), "r")
                    ?.use { true }
                    ?: false
            } catch (_: Exception) {
                false
            }
        }

        return false
    }

    /**
     * Synchronise MediaStore avec Room sans recréer les lignes existantes.
     *
     * - Les lignes existantes sont mises à jour par Upsert, sans suppression/recréation :
     *   les relations playlist -> morceau restent donc intactes.
     * - Un scan vide est considéré comme non fiable et ne modifie jamais la base.
     * - Les morceaux réellement absents sont supprimés uniquement après un scan non vide.
     */
    suspend fun refreshMediaStoreScan(): Int = withContext(Dispatchers.IO) {
        val existingTracks = try {
            audioTrackDao.getAllTracksSnapshot().associateBy { it.id }
        } catch (_: Exception) {
            emptyMap()
        }

        val scannedTracks = scanner.scanAudioFiles()

        if (scannedTracks.isEmpty()) {
            android.util.Log.w(
                "AudioRepository",
                "Scan MediaStore vide : conservation de la bibliothèque Room existante."
            )
            return@withContext existingTracks.size
        }

        val mergedTracks = scannedTracks.map { scanned ->
            val previous = existingTracks[scanned.id]
            if (previous != null) {
                scanned.copy(
                    albumArtUri = scanned.albumArtUri ?: previous.albumArtUri,
                    hasSyncedLyrics = previous.hasSyncedLyrics || scanned.hasSyncedLyrics,
                    lastPlayed = maxOf(previous.lastPlayed, scanned.lastPlayed)
                )
            } else {
                scanned
            }
        }

        audioTrackDao.upsertTracks(mergedTracks)
        audioTrackDao.deleteTracksNotIn(mergedTracks.map { it.id })

        mergedTracks.size
    }

    suspend fun updateLyricsStatus(trackId: Long, hasLyrics: Boolean) = withContext(Dispatchers.IO) {
        try {
            audioTrackDao.updateLyricsStatus(trackId, hasLyrics)
        } catch (e: Exception) {
            android.util.Log.e("AudioRepository", "Erreur updateLyricsStatus: ${e.message}")
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
                val instance = AudioRepository(db.audioTrackDao(), scanner, context)
                INSTANCE = instance
                instance
            }
        }
    }
}
