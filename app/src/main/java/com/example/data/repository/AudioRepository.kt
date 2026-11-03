package com.example.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.data.local.AudioTrackDao
import com.example.data.local.AudioTrackEntity
import com.example.data.local.MusicProDatabase
import com.example.data.scanner.AudioScanner
import com.example.data.scanner.MediaStoreAudioScanner
import kotlinx.coroutines.CancellationException
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
        try {
            audioTrackDao.updateLastPlayed(trackId, timestamp)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Erreur updateLastPlayed: ${e.message}", e)
        }
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
        try {
            audioTrackDao.getTrackCount()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Erreur getTrackCount: ${e.message}", e)
            0
        }
    }

    suspend fun getTrackById(id: Long): AudioTrackEntity? = withContext(Dispatchers.IO) {
        try {
            audioTrackDao.getTrackById(id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Erreur getTrackById: ${e.message}", e)
            null
        }
    }

    suspend fun getAllTracksSnapshot(): List<AudioTrackEntity> = withContext(Dispatchers.IO) {
        try {
            audioTrackDao.getAllTracksSnapshot()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Erreur getAllTracksSnapshot: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Supprime de Room les morceaux dont le fichier n'est plus accessible.
     *
     * Garde-fous (cause de plantages au démarrage) :
     * - ne lève jamais d'exception (appelée au démarrage sans try/catch) ;
     * - si TOUS les morceaux semblent inaccessibles (stockage non monté, autorisation absente,
     *   démarrage juste après une mise à jour), on ne supprime rien : cela viderait la
     *   bibliothèque et les playlists (suppression en cascade) ;
     * - les suppressions sont découpées par lots (limite SQLite de variables par requête).
     */
    suspend fun pruneUnavailableTracks(): Int = withContext(Dispatchers.IO) {
        try {
            val tracks = audioTrackDao.getAllTracksSnapshot()
            if (tracks.isEmpty()) return@withContext 0

            val unavailableIds = tracks
                .filterNot { isTrackAccessible(it) }
                .map { it.id }

            if (unavailableIds.isEmpty()) return@withContext 0

            if (unavailableIds.size == tracks.size) {
                Log.w(TAG, "Tous les morceaux semblent inaccessibles : nettoyage ignoré par sécurité.")
                return@withContext 0
            }

            deleteTracksByIdsInChunks(unavailableIds)
            unavailableIds.size
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Erreur pruneUnavailableTracks: ${e.message}", e)
            0
        }
    }

    private suspend fun deleteTracksByIdsInChunks(ids: Collection<Long>) {
        ids.chunked(DELETE_CHUNK_SIZE).forEach { chunk ->
            audioTrackDao.deleteTracksByIds(chunk)
        }
    }

    /**
     * Mise à jour 059/060 — retire des morceaux de la bibliothèque indexée
     * (suppression fichier gérée par le système via MediaStore en amont).
     */
    suspend fun deleteTracksByIds(ids: Collection<Long>) =
        deleteTracksByIdsInChunks(ids)


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
     * - Les morceaux réellement absents sont supprimés uniquement après un scan non vide,
     *   par lots (pas de requête NOT IN géante qui dépasse la limite de variables SQLite).
     * - Ne lève jamais d'exception : en cas d'erreur, la bibliothèque existante est conservée.
     */
    suspend fun refreshMediaStoreScan(): Int = withContext(Dispatchers.IO) {
        val existingTracks = try {
            audioTrackDao.getAllTracksSnapshot().associateBy { it.id }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            emptyMap()
        }

        try {
            val scannedTracks = scanner.scanAudioFiles()

            if (scannedTracks.isEmpty()) {
                Log.w(
                    TAG,
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
                        lastPlayed = maxOf(previous.lastPlayed, scanned.lastPlayed),
                        genre = scanned.genre.ifBlank { previous.genre }
                    )
                } else {
                    scanned
                }
            }

            audioTrackDao.upsertTracks(mergedTracks)

            val scannedIds = mergedTracks.mapTo(HashSet()) { it.id }
            val staleIds = existingTracks.keys.filterNot { it in scannedIds }
            if (staleIds.isNotEmpty()) {
                deleteTracksByIdsInChunks(staleIds)
            }

            mergedTracks.size
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Erreur refreshMediaStoreScan: ${e.message}", e)
            existingTracks.size
        }
    }

    suspend fun updateLyricsStatus(trackId: Long, hasLyrics: Boolean) = withContext(Dispatchers.IO) {
        try {
            audioTrackDao.updateLyricsStatus(trackId, hasLyrics)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Erreur updateLyricsStatus: ${e.message}")
        }
    }

    suspend fun clearCache() = withContext(Dispatchers.IO) {
        audioTrackDao.clearAllTracks()
    }

    companion object {
        private const val TAG = "AudioRepository"
        private const val DELETE_CHUNK_SIZE = 500

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
