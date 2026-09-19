package com.example.lyrics

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.data.local.AudioTrackEntity
import com.example.lyrics.remote.LrclibClient
import com.example.lyrics.remote.LrclibSearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.File
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.ConcurrentHashMap

class LyricsRepository private constructor(private val context: Context) {

    private val cache = ConcurrentHashMap<Long, LyricsData>()

    suspend fun getLyricsForTrack(track: AudioTrackEntity): LyricsData = withContext(Dispatchers.IO) {
        // 1. En cache mémoire
        cache[track.id]?.let { return@withContext it }

        // 2. Tenter l'extraction via fichier local, jaudiotagger (MP3, FLAC, M4A, OGG) ou .lrc compagnon/privé
        val extracted = Id3SyltReader.extractLyrics(context, track)
        if (extracted != null && extracted.lines.isNotEmpty()) {
            cache[track.id] = extracted
            return@withContext extracted
        }

        // 3. Aucun résultat local
        val empty = LyricsData(source = LyricsSource.NONE)
        cache[track.id] = empty
        empty
    }

    /**
     * Sauvegarde ou importe manuellement des paroles pour une piste.
     */
    fun setLyricsForTrack(trackId: Long, lyricsData: LyricsData) {
        cache[trackId] = lyricsData
    }

    /**
     * Recherche des paroles en ligne sur l'API lrclib.net.
     */
    suspend fun searchLyricsOnline(
        trackTitle: String?,
        artistName: String?,
        durationSec: Int?
    ): Result<List<LrclibSearchResult>> = withContext(Dispatchers.IO) {
        try {
            val cleanTitle = trackTitle?.trim()?.takeIf { it.isNotBlank() }
            val cleanArtist = artistName?.trim()?.takeIf { it.isNotBlank() }

            // 1. Recherche ciblée avec titre, artiste et durée
            var results = LrclibClient.apiService.searchLyrics(
                trackName = cleanTitle,
                artistName = cleanArtist,
                duration = durationSec?.takeIf { it > 0 }
            )

            // 2. Si aucun résultat et qu'on a un titre ou un artiste, tenter une recherche par requête texte
            if (results.isEmpty()) {
                val fallbackQuery = listOfNotNull(cleanTitle, cleanArtist).joinToString(" ").trim()
                if (fallbackQuery.isNotBlank()) {
                    results = LrclibClient.apiService.searchLyrics(query = fallbackQuery)
                }
            }

            Result.success(results)
        } catch (e: UnknownHostException) {
            Result.failure(Exception("Pas de connexion Internet. Veuillez vérifier votre réseau Wi-Fi ou mobile."))
        } catch (e: ConnectException) {
            Result.failure(Exception("Impossible de joindre lrclib.net. Vérifiez votre connexion."))
        } catch (e: SocketTimeoutException) {
            Result.failure(Exception("Délai d'attente dépassé (Timeout). Le serveur lrclib.net met trop de temps à répondre."))
        } catch (e: HttpException) {
            val code = e.code()
            val msg = when (code) {
                404 -> "Aucune ressource trouvée sur lrclib.net (404)."
                500, 502, 503 -> "Le serveur lrclib.net est temporairement indisponible (Code $code)."
                else -> "Erreur du service lrclib.net (Code HTTP $code)."
            }
            Result.failure(Exception(msg))
        } catch (e: Exception) {
            Result.failure(Exception("Erreur réseau : ${e.localizedMessage ?: "Vérifiez votre connexion"}"))
        }
    }

    /**
     * Enregistre le résultat sélectionné (Lrclib) :
     * - Dans les métadonnées internes du fichier (USLT/SYLT pour MP3, LYRICS pour FLAC, ©lyr pour M4A)
     * - En fichier .lrc compagnon si accessible
     * - Dans le cache privé de l'application
     * Met à jour le cache mémoire et retourne le résultat de sauvegarde ainsi que les LyricsData.
     */
    suspend fun applyAndSaveLyrics(
        track: AudioTrackEntity,
        result: LrclibSearchResult
    ): Pair<LyricsSaveResult, LyricsData> = withContext(Dispatchers.IO) {
        val saveResult = Id3SyltWriter.saveLyrics(
            audioPath = track.path,
            result = result,
            fallbackDirectory = context.getExternalFilesDir("lyrics") ?: context.filesDir
        )

        val appliedData = if (!result.syncedLyrics.isNullOrBlank()) {
            val parsed = LrcParser.parse(result.syncedLyrics)
            val source = when (saveResult) {
                is LyricsSaveResult.TagWriteSuccess -> LyricsSource.ID3_SYLT
                is LyricsSaveResult.LrcFileSuccess -> LyricsSource.LRC_FILE
                else -> LyricsSource.LRCLIB_NET
            }
            parsed.copy(
                title = result.displayTitle,
                artist = result.displayArtist,
                album = result.displayAlbum,
                source = source
            )
        } else {
            val lines = (result.plainLyrics ?: "").lines()
                .filter { it.isNotBlank() }
                .mapIndexed { index, text -> LyricLine(index * 3000L, text) }
            LyricsData(
                title = result.displayTitle,
                artist = result.displayArtist,
                album = result.displayAlbum,
                lines = lines,
                source = LyricsSource.ID3_USLT
            )
        }

        cache[track.id] = appliedData
        Pair(saveResult, appliedData)
    }

    /**
     * Enregistre un texte LRC (provenant de Groq Whisper ou d'une saisie manuelle) :
     * - Dans les tags du fichier (USLT/SYLT, FLAC, M4A)
     * - En fichier .lrc compagnon si possible
     * - Dans le cache privé
     */
    suspend fun applyAndSaveLrcText(
        track: AudioTrackEntity,
        lrcText: String
    ): Pair<LyricsSaveResult, LyricsData> = withContext(Dispatchers.IO) {
        val saveResult = Id3SyltWriter.saveLrcText(
            audioPath = track.path,
            lrcContent = lrcText,
            fallbackDirectory = context.getExternalFilesDir("lyrics") ?: context.filesDir,
            customFallbackFileName = "${track.title.ifBlank { "track" }}_${track.id}"
        )

        val parsed = LrcParser.parse(lrcText)
        val source = when (saveResult) {
            is LyricsSaveResult.TagWriteSuccess -> LyricsSource.ID3_SYLT
            is LyricsSaveResult.LrcFileSuccess -> LyricsSource.LRC_FILE
            else -> LyricsSource.GROQ_WHISPER
        }

        val appliedData = parsed.copy(
            title = track.title,
            artist = track.artist,
            album = track.album,
            source = source
        )

        cache[track.id] = appliedData
        Pair(saveResult, appliedData)
    }

    /**
     * Parse et applique une chaîne LRC manuelle pour une piste.
     */
    fun importLrcText(trackId: Long, lrcContent: String): LyricsData {
        val parsed = LrcParser.parse(lrcContent)
        val data = parsed.copy(source = LyricsSource.LRC_FILE)
        cache[trackId] = data
        return data
    }

    companion object {
        @Volatile
        private var instance: LyricsRepository? = null

        fun getInstance(context: Context): LyricsRepository {
            return instance ?: synchronized(this) {
                instance ?: LyricsRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
