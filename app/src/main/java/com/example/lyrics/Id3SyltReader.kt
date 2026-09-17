package com.example.lyrics

import android.util.Log
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.mp3.MP3File
import org.jaudiotagger.tag.id3.AbstractID3v2Frame
import org.jaudiotagger.tag.id3.AbstractID3v2Tag
import org.jaudiotagger.tag.id3.framebody.FrameBodySYLT
import org.jaudiotagger.tag.id3.framebody.FrameBodyUSLT
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.File
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

object Id3SyltReader {

    private const val TAG = "Id3SyltReader"

    /**
     * Tente d'extraire les paroles synchronisées pour un fichier audio donné.
     * Priorité :
     * 1. Fichier .lrc compagnon dans le même dossier
     * 2. Tag ID3 SYLT (Synchronised Lyrics) via jaudiotagger
     * 3. Tag ID3 USLT contenant du format LRC ou texte brut
     */
    fun extractLyrics(file: File): LyricsData? {
        if (!file.exists()) return null

        // 1. Vérification d'un fichier .lrc compagnon
        val companionLrc = File(file.parentFile, "${file.nameWithoutExtension}.lrc")
        if (companionLrc.exists() && companionLrc.isFile) {
            try {
                val data = LrcParser.parse(companionLrc.readText())
                if (data.lines.isNotEmpty()) {
                    return data.copy(source = LyricsSource.LRC_FILE)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Erreur lecture .lrc compagnon: ${e.message}")
            }
        }

        // 2. Lecture via jaudiotagger
        try {
            val audioFile = AudioFileIO.read(file)
            val tag = audioFile.tag

            if (tag is AbstractID3v2Tag) {
                // Recherche du frame SYLT (Synchronised Lyrics / Text)
                val syltData = parseSyltFrame(tag)
                if (syltData != null && syltData.lines.isNotEmpty()) {
                    return syltData
                }

                // Recherche du frame USLT (Unsynchronised Lyrics)
                val usltData = parseUsltFrame(tag)
                if (usltData != null && usltData.lines.isNotEmpty()) {
                    return usltData
                }
            } else if (audioFile is MP3File && audioFile.hasID3v2Tag()) {
                val id3Tag = audioFile.iD3v2Tag
                val syltData = parseSyltFrame(id3Tag)
                if (syltData != null && syltData.lines.isNotEmpty()) {
                    return syltData
                }
            }
        } catch (e: Throwable) {
            Log.w(TAG, "jaudiotagger parsing warning for ${file.name}: ${e.message}")
        }

        return null
    }

    /**
     * Décode le FrameBodySYLT de l'ID3v2 selon la spécification ID3.
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
            Log.w(TAG, "Erreur décodage SYLT: ${e.message}")
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
                // Recherche de la fin de chaîne (0x00)
                val start = i
                while (i < len && bytes[i] != 0.toByte()) {
                    i++
                }

                val text = if (i > start) {
                    String(bytes, start, i - start, StandardCharsets.ISO_8859_1).trim()
                } else {
                    ""
                }

                // Sauter l'octet nul terminateur
                if (i < len && bytes[i] == 0.toByte()) {
                    i++
                }

                // Lecture du timestamp 4-octets big-endian
                if (i + 4 <= len) {
                    val b0 = bytes[i].toLong() and 0xFF
                    val b1 = bytes[i + 1].toLong() and 0xFF
                    val b2 = bytes[i + 2].toLong() and 0xFF
                    val b3 = bytes[i + 3].toLong() and 0xFF
                    val rawTimestamp = (b0 shl 24) or (b1 shl 16) or (b2 shl 8) or b3
                    i += 4

                    val timeMs = if (timeStampFormat == 1) {
                        // MPEG frames -> conversion approximative (~26ms par frame à 44.1kHz)
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
            Log.w(TAG, "Erreur parseSyltBytes: ${e.message}")
        }
        return result.sortedBy { it.timeMs }
    }

    /**
     * Décode le FrameBodyUSLT de l'ID3v2 (paroles non-synchronisées ou format LRC imbriqué).
     */
    private fun parseUsltFrame(tag: AbstractID3v2Tag): LyricsData? {
        try {
            if (!tag.hasFrame("USLT")) return null
            val frame = tag.getFrame("USLT") as? AbstractID3v2Frame ?: return null
            val body = frame.body as? FrameBodyUSLT ?: return null
            val lyricText = body.lyric ?: return null

            if (lyricText.isBlank()) return null

            // Si le texte contient des balises de temps [00:, on utilise le parser LRC
            if (lyricText.contains("[0") || lyricText.contains("[1") || lyricText.contains("[2")) {
                val parsed = LrcParser.parse(lyricText)
                if (parsed.lines.isNotEmpty()) {
                    return parsed.copy(source = LyricsSource.ID3_USLT)
                }
            }

            // Sinon texte brut sans timestamps
            val lines = lyricText.lines()
                .filter { it.isNotBlank() }
                .mapIndexed { index, line ->
                    LyricLine(timeMs = index * 3000L, text = line.trim())
                }

            return LyricsData(
                lines = lines,
                source = LyricsSource.ID3_USLT
            )
        } catch (e: Exception) {
            Log.w(TAG, "Erreur USLT: ${e.message}")
        }
        return null
    }
}
