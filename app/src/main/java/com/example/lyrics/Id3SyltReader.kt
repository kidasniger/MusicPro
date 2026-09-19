package com.example.lyrics

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.data.local.AudioTrackEntity
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.mp3.MP3File
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.id3.AbstractID3v2Frame
import org.jaudiotagger.tag.id3.AbstractID3v2Tag
import org.jaudiotagger.tag.id3.framebody.FrameBodySYLT
import org.jaudiotagger.tag.id3.framebody.FrameBodyTXXX
import org.jaudiotagger.tag.id3.framebody.FrameBodyUSLT
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

object Id3SyltReader {

    private const val TAG = "Id3SyltReader"

    private fun logD(tag: String, msg: String) {
        try { android.util.Log.d(tag, msg) } catch (_: Throwable) {}
    }
    private fun logW(tag: String, msg: String) {
        try { android.util.Log.w(tag, msg) } catch (_: Throwable) { System.err.println("[$tag] $msg") }
    }

    /**
     * Tente d'extraire les paroles synchronisées pour une piste audio :
     * 1. Fichier .lrc compagnon dans le dossier d'origine
     * 2. Fichier .lrc dans le cache/dossier privé de l'application
     * 3. Analyse approfondie des métadonnées du fichier audio (MP3, FLAC, M4A, OGG, etc.)
     */
    fun extractLyrics(context: Context, track: AudioTrackEntity): LyricsData? {
        val path = track.path

        // 1. Vérification d'un fichier .lrc compagnon dans le même dossier
        if (!path.isNullOrBlank()) {
            try {
                val audioFile = File(path)
                val companionLrc = File(audioFile.parentFile, "${audioFile.nameWithoutExtension}.lrc")
                if (companionLrc.exists() && companionLrc.isFile) {
                    val content = companionLrc.readText(StandardCharsets.UTF_8)
                    val parsed = LrcParser.parse(content)
                    if (parsed.lines.isNotEmpty()) {
                        return parsed.copy(
                            title = track.title,
                            artist = track.artist,
                            album = track.album,
                            source = LyricsSource.LRC_FILE
                        )
                    }
                }
            } catch (e: Exception) {
                logD(TAG, "Vérification .lrc local: ${e.message}")
            }
        }

        // 2. Vérification dans le stockage privé de l'application
        try {
            val appLyricsDirs = listOfNotNull(
                context.getExternalFilesDir("lyrics"),
                File(context.filesDir, "lyrics"),
                context.filesDir
            )
            val candidateNames = listOf(
                "${track.id}.lrc",
                "track_${track.id}.lrc",
                "${track.title}_${track.id}.lrc",
                "${File(path ?: "").nameWithoutExtension}.lrc"
            )

            for (dir in appLyricsDirs) {
                if (dir.exists()) {
                    for (name in candidateNames) {
                        val cachedFile = File(dir, name)
                        if (cachedFile.exists() && cachedFile.length() > 0) {
                            val content = cachedFile.readText(StandardCharsets.UTF_8)
                            val parsed = LrcParser.parse(content)
                            if (parsed.lines.isNotEmpty()) {
                                return parsed.copy(
                                    title = track.title,
                                    artist = track.artist,
                                    album = track.album,
                                    source = LyricsSource.LRC_FILE
                                )
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            logD(TAG, "Vérification cache privé lyrics: ${e.message}")
        }

        // 3. Extraction depuis les métadonnées internes du fichier audio (MP3, FLAC, M4A, OGG...)
        var targetFile: File? = null
        var isTempFile = false

        try {
            if (!path.isNullOrBlank()) {
                val directFile = File(path)
                if (directFile.exists() && directFile.canRead()) {
                    targetFile = directFile
                }
            }

            // Si l'accès direct par chemin est bloqué par Android Scoped Storage,
            // on copie le flux audio vers un fichier temporaire dans le cache de l'app
            if (targetFile == null && !track.contentUri.isNullOrBlank()) {
                val uri = Uri.parse(track.contentUri)
                val ext = if (!path.isNullOrBlank()) File(path).extension.ifBlank { "mp3" } else "mp3"
                val temp = File(context.cacheDir, "tag_reader_${track.id}.$ext")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(temp).use { output ->
                        input.copyTo(output)
                    }
                }
                if (temp.exists() && temp.length() > 0) {
                    targetFile = temp
                    isTempFile = true
                }
            }

            if (targetFile != null && targetFile.exists()) {
                val data = extractFromAudioFile(targetFile, track)
                if (data != null && data.lines.isNotEmpty()) {
                    return data
                }
            }
        } catch (e: Throwable) {
            logW(TAG, "Erreur lecture des métadonnées audio: ${e.message}")
        } finally {
            if (isTempFile && targetFile != null) {
                try {
                    targetFile.delete()
                } catch (_: Exception) {}
            }
        }

        return null
    }

    /**
     * Analyse en profondeur les tags audio avec jaudiotagger pour tous les conteneurs (MP3, FLAC, M4A, OGG).
     */
    private fun extractFromAudioFile(file: File, track: AudioTrackEntity): LyricsData? {
        try {
            val audioFile = AudioFileIO.read(file)
            val tag = audioFile.tag

            // 1. Tag ID3v2 SYLT (Synchronised Lyrics binaire millisecondes)
            if (tag is AbstractID3v2Tag) {
                val syltData = parseSyltFrame(tag)
                if (syltData != null && syltData.lines.isNotEmpty()) {
                    return syltData.copy(
                        title = track.title,
                        artist = track.artist,
                        album = track.album
                    )
                }
            } else if (audioFile is MP3File && audioFile.hasID3v2Tag()) {
                val syltData = parseSyltFrame(audioFile.iD3v2Tag)
                if (syltData != null && syltData.lines.isNotEmpty()) {
                    return syltData.copy(
                        title = track.title,
                        artist = track.artist,
                        album = track.album
                    )
                }
            }

            // 2. Tag ID3v2 USLT (Unsynchronised Lyrics avec format LRC ou texte)
            if (tag is AbstractID3v2Tag) {
                val usltData = parseUsltFrame(tag)
                if (usltData != null && usltData.lines.isNotEmpty()) {
                    return usltData.copy(
                        title = track.title,
                        artist = track.artist,
                        album = track.album
                    )
                }

                // Recherche dans les tags TXXX personnalisés (ex: TXXX:LYRICS, TXXX:SYNCED LYRICS)
                val txxxData = parseTxxxLyrics(tag)
                if (txxxData != null && txxxData.lines.isNotEmpty()) {
                    return txxxData.copy(
                        title = track.title,
                        artist = track.artist,
                        album = track.album
                    )
                }
            }

            // 3. Tag Universel jaudiotagger (FLAC, M4A/AAC, OGG, WMA, MP3)
            if (tag != null) {
                val universalLyrics = try {
                    tag.getFirst(FieldKey.LYRICS)
                } catch (_: Exception) {
                    null
                }

                if (!universalLyrics.isNullOrBlank()) {
                    return parseLyricsString(universalLyrics, LyricsSource.ID3_USLT).copy(
                        title = track.title,
                        artist = track.artist,
                        album = track.album
                    )
                }

                // Recherche spécifique Vorbis Comment (FLAC / OGG)
                val vorbisKeys = listOf("LYRICS", "SYNCEDLYRICS", "UNSYNCEDLYRICS")
                for (key in vorbisKeys) {
                    val raw = try {
                        val fields = tag.getFields(key)
                        fields.firstOrNull()?.toString()
                    } catch (_: Exception) {
                        null
                    }
                    if (!raw.isNullOrBlank()) {
                        return parseLyricsString(raw, LyricsSource.ID3_USLT).copy(
                            title = track.title,
                            artist = track.artist,
                            album = track.album
                        )
                    }
                }
            }
        } catch (e: Throwable) {
            logD(TAG, "extractFromAudioFile note: ${e.message}")
        }
        return null
    }

    /**
     * Parse une chaîne de paroles, qu'elle soit au format LRC avec timestamps [mm:ss.xx]
     * ou sous forme de texte brut sans timestamps.
     */
    private fun parseLyricsString(text: String, source: LyricsSource): LyricsData {
        val trimmed = text.trim()
        val hasTimestamps = trimmed.contains(Regex("\\[\\d{1,2}:\\d{2}"))
        if (hasTimestamps) {
            val parsed = LrcParser.parse(trimmed)
            if (parsed.lines.isNotEmpty()) {
                return parsed.copy(source = source)
            }
        }

        // Texte brut sans timestamps
        val lines = trimmed.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .mapIndexed { index, line ->
                LyricLine(timeMs = index * 3000L, text = line)
            }

        return LyricsData(
            lines = lines,
            source = source
        )
    }

    /**
     * Décode le FrameBodySYLT de l'ID3v2.
     */
    private fun parseSyltFrame(tag: AbstractID3v2Tag): LyricsData? {
        try {
            if (!tag.hasFrame("SYLT")) return null
            val frame = tag.getFrame("SYLT") as? AbstractID3v2Frame ?: return null
            val body = frame.body as? FrameBodySYLT ?: return null

            val rawBytes = body.lyrics ?: return null
            if (rawBytes.isEmpty()) return null

            val timeStampFormat = body.timeStampFormat // 1 = MPEG frames, 2 = Milliseconds
            val lines = parseSyltBytes(rawBytes, timeStampFormat)

            if (lines.isNotEmpty()) {
                return LyricsData(
                    lines = lines,
                    source = LyricsSource.ID3_SYLT
                )
            }
        } catch (e: Exception) {
            logW(TAG, "Erreur décodage SYLT: ${e.message}")
        }
        return null
    }

    /**
     * Décode le FrameBodyUSLT de l'ID3v2.
     */
    private fun parseUsltFrame(tag: AbstractID3v2Tag): LyricsData? {
        try {
            if (!tag.hasFrame("USLT")) return null
            val frame = tag.getFrame("USLT") as? AbstractID3v2Frame ?: return null
            val body = frame.body as? FrameBodyUSLT ?: return null
            val lyricText = body.lyric ?: return null

            if (lyricText.isBlank()) return null
            return parseLyricsString(lyricText, LyricsSource.ID3_USLT)
        } catch (e: Exception) {
            logW(TAG, "Erreur USLT: ${e.message}")
        }
        return null
    }

    /**
     * Recherche de paroles dans les champs personnalisés TXXX (ex: TXXX:LYRICS, TXXX:SYNCED LYRICS).
     */
    private fun parseTxxxLyrics(tag: AbstractID3v2Tag): LyricsData? {
        try {
            val iterator = tag.iterator()
            while (iterator.hasNext()) {
                val obj = iterator.next()
                if (obj is AbstractID3v2Frame && obj.identifier == "TXXX") {
                    val body = obj.body as? FrameBodyTXXX ?: continue
                    val desc = body.description?.uppercase() ?: ""
                    if (desc.contains("LYRICS") || desc.contains("SYNCED")) {
                        val text = body.text ?: continue
                        if (text.isNotBlank()) {
                            return parseLyricsString(text, LyricsSource.ID3_USLT)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            logD(TAG, "TXXX lyrics check: ${e.message}")
        }
        return null
    }

    /**
     * Analyse binaire des entrées SYLT (chaîne terminée par 0x00 suivie de 4 octets de timestamp).
     */
    private fun parseSyltBytes(bytes: ByteArray, timeStampFormat: Int): List<LyricLine> {
        val result = mutableListOf<LyricLine>()
        try {
            var i = 0
            val len = bytes.size

            while (i < len) {
                val start = i
                while (i < len && bytes[i] != 0.toByte()) {
                    i++
                }

                val text = if (i > start) {
                    String(bytes, start, i - start, StandardCharsets.ISO_8859_1).trim()
                } else {
                    ""
                }

                if (i < len && bytes[i] == 0.toByte()) {
                    i++
                }

                if (i + 4 <= len) {
                    val b0 = bytes[i].toLong() and 0xFF
                    val b1 = bytes[i + 1].toLong() and 0xFF
                    val b2 = bytes[i + 2].toLong() and 0xFF
                    val b3 = bytes[i + 3].toLong() and 0xFF
                    val rawTimestamp = (b0 shl 24) or (b1 shl 16) or (b2 shl 8) or b3
                    i += 4

                    val timeMs = if (timeStampFormat == 1) {
                        (rawTimestamp * 26.12).toLong()
                    } else {
                        rawTimestamp
                    }

                    if (text.isNotEmpty() || timeMs > 0) {
                        result.add(LyricLine(timeMs = timeMs, text = text))
                    }
                } else {
                    break
                }
            }
        } catch (e: Exception) {
            logW(TAG, "Erreur parseSyltBytes: ${e.message}")
        }
        return result.sortedBy { it.timeMs }
    }
}
