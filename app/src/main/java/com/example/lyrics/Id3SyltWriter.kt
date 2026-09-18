package com.example.lyrics

import android.util.Log
import com.example.lyrics.remote.LrclibSearchResult
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.mp3.MP3File
import org.jaudiotagger.tag.id3.AbstractID3v2Tag
import org.jaudiotagger.tag.id3.ID3v23Frame
import org.jaudiotagger.tag.id3.ID3v23Tag
import org.jaudiotagger.tag.id3.ID3v24Frame
import org.jaudiotagger.tag.id3.ID3v24Tag
import org.jaudiotagger.tag.id3.framebody.FrameBodySYLT
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets

sealed class LyricsSaveResult {
    data class Id3SyltSuccess(val path: String, val linesCount: Int) : LyricsSaveResult()
    data class LrcFileSuccess(val lrcPath: String, val linesCount: Int) : LyricsSaveResult()
    data class Error(val message: String) : LyricsSaveResult()
}

object Id3SyltWriter {

    private const val TAG = "Id3SyltWriter"

    /**
     * Enregistre les paroles trouvées pour une piste audio :
     * 1. Si le format le permet (ex. fichier MP3 avec tag ID3), écrit le tag ID3 SYLT via jaudiotagger.
     * 2. Sinon (autre format FLAC, M4A, OGG, WAV ou erreur tag), enregistre un fichier .lrc compagnon
     *    à côté du fichier audio dans le même dossier.
     */
    fun saveLyrics(
        audioPath: String?,
        result: LrclibSearchResult,
        fallbackDirectory: File? = null
    ): LyricsSaveResult {
        val lrcContent = result.syncedLyrics ?: result.plainLyrics ?: ""
        return saveLrcText(
            audioPath = audioPath,
            lrcContent = lrcContent,
            fallbackDirectory = fallbackDirectory,
            customFallbackFileName = "lyrics_${result.id ?: System.currentTimeMillis()}"
        )
    }

    /**
     * Enregistre un texte au format LRC (généré par Groq Whisper ou importé manuellement) :
     * Tente l'écriture ID3 SYLT si MP3, sinon sauvegarde sous forme de fichier .lrc compagnon.
     */
    fun saveLrcText(
        audioPath: String?,
        lrcContent: String,
        fallbackDirectory: File? = null,
        customFallbackFileName: String? = null
    ): LyricsSaveResult {
        if (lrcContent.isBlank()) {
            return LyricsSaveResult.Error("Aucune parole disponible à enregistrer.")
        }

        // Parsing des lignes pour le format SYLT
        val parsedData = LrcParser.parse(lrcContent)

        if (audioPath.isNullOrBlank()) {
            return writeLrcCompanion(
                directory = fallbackDirectory,
                fileName = customFallbackFileName ?: "lyrics_${System.currentTimeMillis()}",
                content = lrcContent,
                linesCount = parsedData.lines.size
            )
        }

        val audioFile = File(audioPath)

        // 1. Tenter d'écrire le tag ID3 SYLT si le fichier est un MP3 existant et inscriptible
        if (parsedData.lines.isNotEmpty() && canSupportId3Sylt(audioFile)) {
            val syltAttempt = tryWriteSylt(audioFile, parsedData.lines)
            if (syltAttempt is LyricsSaveResult.Id3SyltSuccess) {
                Log.d(TAG, "Paroles enregistrées en tag ID3 SYLT dans ${audioFile.name}")
                return syltAttempt
            }
            Log.w(TAG, "Échec écriture ID3 SYLT (${(syltAttempt as? LyricsSaveResult.Error)?.message}), bascule vers .lrc")
        }

        // 2. Sinon sauvegarder un fichier .lrc à côté du fichier audio dans le même dossier
        val parentDir = audioFile.parentFile ?: fallbackDirectory
        val baseName = audioFile.nameWithoutExtension.ifBlank { "track" }

        return writeLrcCompanion(
            directory = parentDir,
            fileName = baseName,
            content = lrcContent,
            linesCount = parsedData.lines.size
        )
    }

    private fun canSupportId3Sylt(file: File): Boolean {
        if (!file.exists()) return false
        val ext = file.extension.lowercase()
        return ext == "mp3"
    }

    private fun tryWriteSylt(file: File, lines: List<LyricLine>): LyricsSaveResult {
        return try {
            val audioFile = AudioFileIO.read(file)
            val syltBytes = serializeSyltBytes(lines)

            if (audioFile is MP3File) {
                var id3Tag = audioFile.iD3v2Tag
                if (id3Tag == null) {
                    id3Tag = ID3v23Tag()
                    audioFile.iD3v2Tag = id3Tag
                }
                val frame = if (id3Tag is ID3v24Tag) {
                    ID3v24Frame("SYLT").apply {
                        body = FrameBodySYLT(0, "eng", 2, 1, "", syltBytes)
                    }
                } else {
                    ID3v23Frame("SYLT").apply {
                        body = FrameBodySYLT(0, "eng", 2, 1, "", syltBytes)
                    }
                }
                id3Tag.setFrame(frame)
                audioFile.commit()
                LyricsSaveResult.Id3SyltSuccess(file.absolutePath, lines.size)
            } else {
                val tag = audioFile.tag
                if (tag is AbstractID3v2Tag) {
                    val frame = if (tag is ID3v24Tag) {
                        ID3v24Frame("SYLT").apply {
                            body = FrameBodySYLT(0, "eng", 2, 1, "", syltBytes)
                        }
                    } else {
                        ID3v23Frame("SYLT").apply {
                            body = FrameBodySYLT(0, "eng", 2, 1, "", syltBytes)
                        }
                    }
                    tag.setFrame(frame)
                    audioFile.commit()
                    LyricsSaveResult.Id3SyltSuccess(file.absolutePath, lines.size)
                } else {
                    LyricsSaveResult.Error("Format ${file.extension} incompatible avec tag ID3v2 SYLT")
                }
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Erreur lors de l'écriture jaudiotagger ID3 SYLT : ${e.message}")
            LyricsSaveResult.Error(e.message ?: "Erreur jaudiotagger ID3 SYLT")
        }
    }

    /**
     * Encode les lignes de paroles au format binaire ID3 SYLT :
     * Chaîne terminée par 0x00 + 4 octets timestamp big-endian (millisecondes).
     */
    private fun serializeSyltBytes(lines: List<LyricLine>): ByteArray {
        val stream = ByteArrayOutputStream()
        for (line in lines) {
            val textBytes = line.text.toByteArray(StandardCharsets.ISO_8859_1)
            stream.write(textBytes)
            stream.write(0) // 0x00 null terminator
            val ms = line.timeMs.coerceAtLeast(0L)
            stream.write(((ms ushr 24) and 0xFF).toInt())
            stream.write(((ms ushr 16) and 0xFF).toInt())
            stream.write(((ms ushr 8) and 0xFF).toInt())
            stream.write((ms and 0xFF).toInt())
        }
        return stream.toByteArray()
    }

    /**
     * Sauvegarde un fichier .lrc compagnon dans le dossier indiqué.
     */
    private fun writeLrcCompanion(
        directory: File?,
        fileName: String,
        content: String,
        linesCount: Int
    ): LyricsSaveResult {
        return try {
            val dir = directory ?: return LyricsSaveResult.Error("Dossier de destination introuvable.")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val lrcFile = File(dir, "$fileName.lrc")
            lrcFile.writeText(content, StandardCharsets.UTF_8)
            LyricsSaveResult.LrcFileSuccess(lrcFile.absolutePath, linesCount)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur écriture fichier .lrc compagnon : ${e.message}", e)
            LyricsSaveResult.Error("Impossible d'écrire le fichier .lrc : ${e.localizedMessage}")
        }
    }
}
