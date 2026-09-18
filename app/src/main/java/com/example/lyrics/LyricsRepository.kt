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

        // 2. Tenter l'extraction via fichier local et jaudiotagger / .lrc compagnon
        if (!track.path.isNullOrBlank()) {
            val file = File(track.path)
            if (file.exists()) {
                val extracted = Id3SyltReader.extractLyrics(file)
                if (extracted != null && extracted.lines.isNotEmpty()) {
                    cache[track.id] = extracted
                    return@withContext extracted
                }
            }
        }

        // 3. Si aucun fichier local ou pas de tag trouvé, vérifier si des paroles de démo correspondent
        val demoLyrics = getDemoLyricsForTitle(track.title, track.artist)
        if (demoLyrics != null) {
            cache[track.id] = demoLyrics
            return@withContext demoLyrics
        }

        // 4. Aucun résultat
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
     * Gère les erreurs réseau (pas de connexion, timeout, erreur HTTP) avec des messages explicites.
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
     * Enregistre le résultat sélectionné :
     * - En tag ID3 SYLT si le fichier audio le supporte (ex: MP3).
     * - Sinon dans un fichier .lrc compagnon dans le même dossier.
     * Met à jour le cache et retourne le résultat de sauvegarde ainsi que les LyricsData appliquées.
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
            val source = if (saveResult is LyricsSaveResult.Id3SyltSuccess) {
                LyricsSource.ID3_SYLT
            } else {
                LyricsSource.LRC_FILE
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
     * Enregistre un texte LRC (provenant de Groq Whisper ou d'une saisie) :
     * - En tag ID3 SYLT si MP3 supporté
     * - Sinon dans un fichier .lrc compagnon dans le même dossier
     * Met à jour le cache et retourne le résultat de sauvegarde ainsi que les LyricsData.
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
        val source = if (saveResult is LyricsSaveResult.Id3SyltSuccess) {
            LyricsSource.ID3_SYLT
        } else {
            LyricsSource.LRC_FILE
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

    /**
     * Génère des paroles synchronisées de démonstration pour un morceau donné.
     */
    fun generateDemoLyrics(track: AudioTrackEntity): LyricsData {
        val title = track.title
        val artist = track.artist
        val durationMs = if (track.duration > 0) track.duration else 180_000L

        val lines = listOf(
            LyricLine(0L, "♪ Introduction synthétique ♪"),
            LyricLine(12_000L, "Dans les lueurs violettes de la métropole"),
            LyricLine(24_000L, "Les signaux néon dansent au rythme des basses"),
            LyricLine(36_000L, "La nuit s'illumine sous nos pas pressés"),
            LyricLine(48_000L, "MusicPro pulse dans les veines de la ville"),
            LyricLine(60_000L, "♪ Montée en fréquence cyan ♪"),
            LyricLine(72_000L, "Écho quantique, voyage sans fin"),
            LyricLine(85_000L, "Rien ne peut arrêter cette symphonie"),
            LyricLine(98_000L, "Les ondes traversent les gratte-ciels en verre"),
            LyricLine(112_000L, "Nous sommes connectés à l'infini"),
            LyricLine(125_000L, "♪ Solo de synthétiseur rétro ♪"),
            LyricLine(145_000L, "Refrain néon au cœur de la nuit"),
            LyricLine(160_000L, "La musique résonne pour toujours"),
            LyricLine(175_000L, "♪ Fade out progressif ♪")
        ).filter { it.timeMs < durationMs }

        val data = LyricsData(
            title = title,
            artist = artist,
            lines = lines,
            source = LyricsSource.EMBEDDED_DEMO
        )
        cache[track.id] = data
        return data
    }

    private fun getDemoLyricsForTitle(title: String, artist: String): LyricsData? {
        val t = title.lowercase()
        return when {
            t.contains("cyber") || t.contains("synth") -> {
                LrcParser.parse(
                    """
                    [ti:Cyber City Lights]
                    [ar:Neon Wave]
                    [al:Synthwave Odyssey]
                    [00:00.00]♪ Arpégiateur d'introduction ♪
                    [00:06.00]Plein phare sur les avenues désertes
                    [00:12.50]Le reflet des néons brille sur le bitume mouillé
                    [00:19.00]Un battement régulier guide la trajectoire
                    [00:26.20]Cyber City Lights, guide nos mémoires
                    [00:33.00]♪ Montée en puissance - 120 BPM ♪
                    [00:41.00]Fréquences pures dans le ciel nocturne
                    [00:48.50]Vitesse constante à travers le réseau
                    [00:55.20]Les synthétiseurs vibrent en harmonie
                    [01:03.00]Respire le souffle de l'aurore électrique
                    [01:11.50]♪ Refrain Principal ♪
                    [01:18.00]Dans la matrice de nos rêves
                    [01:25.00]Tout s'efface pour laisser place au son
                    [01:32.40]Cyber City Lights ne s'éteindra jamais
                    [01:42.00]♪ Solo analogique et reverb spatiale ♪
                    [02:00.00]L'écho s'éloigne vers l'horizon violet
                    [02:15.00]♪ Fin du voyage ♪
                    """.trimIndent()
                ).copy(source = LyricsSource.EMBEDDED_DEMO)
            }
            t.contains("midnight") || t.contains("drive") -> {
                LrcParser.parse(
                    """
                    [ti:Midnight Drive]
                    [ar:Retro Future]
                    [al:Nightcall Sessions]
                    [00:00.00]♪ Clavier feutré et basse analogique ♪
                    [00:08.00]Autoroute sans fin sous un ciel étoilé
                    [00:16.00]Le compteur grimpe, l'esprit s'évade
                    [00:25.00]Minuit sonne dans le rétroviseur
                    [00:34.50]Plus aucune voix, juste les accords majeurs
                    [00:43.00]♪ Rythmique lo-fi envoûtante ♪
                    [00:52.00]Une ombre violette file sur la voie
                    [01:01.00]La ville s'endort, notre voyage commence
                    [01:10.50]Garde les yeux fixés sur la ligne d'horizon
                    [01:20.00]Midnight Drive, vers l'éternel sanctuaire
                    [01:35.00]♪ Thème de saxophone néon ♪
                    [01:50.00]Le jour ne viendra pas nous déranger
                    [02:05.00]♪ Décélération en douceur ♪
                    """.trimIndent()
                ).copy(source = LyricsSource.EMBEDDED_DEMO)
            }
            t.contains("horizon") || t.contains("quantum") || t.contains("electric") -> {
                LrcParser.parse(
                    """
                    [ti:Neon Horizon]
                    [ar:Electro Pulse]
                    [00:00.00]♪ Nappe atmosphérique futuriste ♪
                    [00:07.50]Des impulsions lumineuses parcourent le ciel
                    [00:15.00]Un monde nouveau s'ouvre devant nous
                    [00:23.00]Vibration cyan, chaleur électrique
                    [00:31.20]L'onde sonore repousse les frontières
                    [00:39.00]♪ Drop électro intense ♪
                    [00:47.00]Connecte ton esprit au signal
                    [00:55.00]Le flux ne s'interrompt jamais
                    [01:04.00]Neon Horizon, notre destination
                    [01:13.00]Chaque note résonne plus fort
                    [01:22.00]♪ Climax mélodique ♪
                    [01:35.00]Vers le point zéro de la musique
                    """.trimIndent()
                ).copy(source = LyricsSource.EMBEDDED_DEMO)
            }
            else -> null
        }
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
