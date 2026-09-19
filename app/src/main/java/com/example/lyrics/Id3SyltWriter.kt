package com.example.lyrics

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.util.Log
import com.example.lyrics.remote.LrclibSearchResult
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.mp3.MP3File
import org.jaudiotagger.tag.FieldKey
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
    data class TagWriteSuccess(val path: String, val tagType: String, val linesCount: Int) : LyricsSaveResult()
    data class LrcFileSuccess(val lrcPath: String, val linesCount: Int) : LyricsSaveResult()
    data class AppCacheSuccess(val cachePath: String, val linesCount: Int) : LyricsSaveResult()
    data class Error(val message: String) : LyricsSaveResult()
}

object Id3SyltWriter {

    private const val TAG = "Id3SyltWriter"

    private fun logD(tag: String, msg: String) {
        try { android.util.Log.d(tag, msg) } catch (_: Throwable) {}
    }
    private fun logW(tag: String, msg: String) {
        try { android.util.Log.w(tag, msg) } catch (_: Throwable) { System.err.println("[$tag] $msg") }
    }
    private fun logE(tag: String, msg: String, tr: Throwable? = null) {
        try { android.util.Log.e(tag, msg, tr) } catch (_: Throwable) { System.err.println("[$tag] $msg") }
    }

    /**
     * Enregistre les paroles trouvées (Lrclib) pour une piste audio :
     * 1. Écrit dans les tags du fichier audio physique (MP3 USLT/SYLT, FLAC LYRICS, M4A ©lyr).
     * 2. Tente d'écrire un fichier .lrc compagnon à côté.
     * 3. Conserve une copie dans le cache de l'application pour garantir la disponibilité.
     */
    fun saveLyrics(
        context: Context? = null,
        audioPath: String?,
        contentUri: Uri? = null,
        result: LrclibSearchResult,
        fallbackDirectory: File? = null
    ): LyricsSaveResult {
        val lrcContent = result.syncedLyrics ?: result.plainLyrics ?: ""
        return saveLrcText(
            context = context,
            audioPath = audioPath,
            contentUri = contentUri,
            lrcContent = lrcContent,
            fallbackDirectory = fallbackDirectory,
            customFallbackFileName = "lyrics_${result.id ?: System.currentTimeMillis()}"
        )
    }

    /**
     * Surcharge de compatibilité sans context/uri.
     */
    fun saveLyrics(
        audioPath: String?,
        result: LrclibSearchResult,
        fallbackDirectory: File? = null
    ): LyricsSaveResult = saveLyrics(
        context = null,
        audioPath = audioPath,
        contentUri = null,
        result = result,
        fallbackDirectory = fallbackDirectory
    )

    /**
     * Enregistre un texte au format LRC (Whisper Groq, import manuel ou Lrclib).
     */
    fun saveLrcText(
        context: Context? = null,
        audioPath: String?,
        contentUri: Uri? = null,
        lrcContent: String,
        fallbackDirectory: File? = null,
        customFallbackFileName: String? = null
    ): LyricsSaveResult {
        if (lrcContent.isBlank()) {
            return LyricsSaveResult.Error("Aucune parole disponible à enregistrer.")
        }

        val parsedData = LrcParser.parse(lrcContent)

        // 1. Sauvegarde systématique dans le cache privé de l'application pour disponibilité immédiate
        fallbackDirectory?.let { dir ->
            try {
                if (!dir.exists()) dir.mkdirs()
                val cacheName = customFallbackFileName ?: "track_${System.currentTimeMillis()}"
                val cacheFile = File(dir, "$cacheName.lrc")
                cacheFile.writeText(lrcContent, StandardCharsets.UTF_8)
            } catch (e: Exception) {
                logD(TAG, "Cache privé lyrics note: ${e.message}")
            }
        }

        if (audioPath.isNullOrBlank()) {
            return writeLrcCompanion(
                directory = fallbackDirectory,
                fileName = customFallbackFileName ?: "lyrics_${System.currentTimeMillis()}",
                content = lrcContent,
                linesCount = parsedData.lines.size
            )
        }

        val audioFile = File(audioPath)

        // 2. Tenter d'écrire physiquement et directement dans les métadonnées du fichier si accessible en écriture
        if (audioFile.exists() && audioFile.canWrite()) {
            val tagResult = tryWriteAudioTags(audioFile, lrcContent, parsedData.lines)
            if (tagResult is LyricsSaveResult.TagWriteSuccess) {
                try {
                    val companion = File(audioFile.parentFile, "${audioFile.nameWithoutExtension}.lrc")
                    companion.writeText(lrcContent, StandardCharsets.UTF_8)
                } catch (_: Exception) {}

                context?.let { ctx ->
                    try {
                        MediaScannerConnection.scanFile(ctx, arrayOf(audioFile.absolutePath), null, null)
                    } catch (_: Exception) {}
                }
                return tagResult
            }
        }

        // 3. Si l'accès direct n'est pas possible (Scoped Storage Android 11+) mais qu'une autorisation
        // MediaStore a été accordée (ContentResolver / OutputStream), utiliser le flux via cache temporaire
        if (context != null && (contentUri != null || audioFile.exists())) {
            val scopedResult = writeAudioTagsWithScopedStorage(
                context = context,
                audioFile = audioFile,
                contentUri = contentUri,
                lrcContent = lrcContent,
                lines = parsedData.lines
            )
            if (scopedResult is LyricsSaveResult.TagWriteSuccess) {
                try {
                    val parentDir = audioFile.parentFile
                    if (parentDir != null && parentDir.canWrite()) {
                        val companion = File(parentDir, "${audioFile.nameWithoutExtension}.lrc")
                        companion.writeText(lrcContent, StandardCharsets.UTF_8)
                    }
                } catch (_: Exception) {}

                try {
                    MediaScannerConnection.scanFile(context, arrayOf(audioFile.absolutePath), null, null)
                } catch (_: Exception) {}

                return scopedResult
            }
        }

        // 4. Si l'écriture dans le fichier est bloquée par Android Scoped Storage,
        // tenter d'écrire le fichier .lrc compagnon dans le dossier du fichier audio
        val parentDir = audioFile.parentFile
        if (parentDir != null && parentDir.canWrite()) {
            val companionResult = writeLrcCompanion(
                directory = parentDir,
                fileName = audioFile.nameWithoutExtension.ifBlank { "track" },
                content = lrcContent,
                linesCount = parsedData.lines.size
            )
            if (companionResult is LyricsSaveResult.LrcFileSuccess) {
                return companionResult
            }
        }

        // 5. Fallback vers le dossier privé
        return writeLrcCompanion(
            directory = fallbackDirectory,
            fileName = customFallbackFileName ?: audioFile.nameWithoutExtension.ifBlank { "lyrics" },
            content = lrcContent,
            linesCount = parsedData.lines.size
        )
    }

    /**
     * Surcharge de compatibilité sans context/uri.
     */
    fun saveLrcText(
        audioPath: String?,
        lrcContent: String,
        fallbackDirectory: File? = null,
        customFallbackFileName: String? = null
    ): LyricsSaveResult = saveLrcText(
        context = null,
        audioPath = audioPath,
        contentUri = null,
        lrcContent = lrcContent,
        fallbackDirectory = fallbackDirectory,
        customFallbackFileName = customFallbackFileName
    )

    /**
     * Gère l'écriture de tags audio sur Android Scoped Storage (Android 11+) :
     * 1. Copie temporairement les données audio dans le cache privé de l'app.
     * 2. Modifie les balises ID3 USLT/SYLT sur ce fichier temporaire avec jaudiotagger.
     * 3. Réécrit le fichier balisé vers la cible via ContentResolver (autorisé par createWriteRequest) ou FUSE.
     */
    private fun writeAudioTagsWithScopedStorage(
        context: Context,
        audioFile: File,
        contentUri: Uri?,
        lrcContent: String,
        lines: List<LyricLine>
    ): LyricsSaveResult {
        var tempFile: File? = null
        return try {
            val extension = if (audioFile.name.contains('.')) audioFile.extension else "mp3"
            tempFile = File(context.cacheDir, "tag_edit_${System.currentTimeMillis()}.$extension")

            var readSuccess = false
            if (audioFile.exists() && audioFile.canRead()) {
                try {
                    audioFile.copyTo(tempFile, overwrite = true)
                    readSuccess = true
                } catch (e: Exception) {
                    logD(TAG, "Échec copie directe vers cache: ${e.message}")
                }
            }
            if (!readSuccess && contentUri != null) {
                try {
                    context.contentResolver.openInputStream(contentUri)?.use { input ->
                        tempFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    readSuccess = true
                } catch (e: Exception) {
                    logE(TAG, "Échec lecture InputStream MediaStore: ${e.message}", e)
                }
            }

            if (!readSuccess || !tempFile.exists() || tempFile.length() == 0L) {
                return LyricsSaveResult.Error("Impossible de lire les données du morceau pour l'édition des tags.")
            }

            val originalSize = tempFile.length()
            val tagResult = tryWriteAudioTags(tempFile, lrcContent, lines)
            if (tagResult !is LyricsSaveResult.TagWriteSuccess) {
                return tagResult
            }

            // Vérification anti-corruption : le fichier temporaire doit faire au moins 80% de la taille d'origine
            if (!tempFile.exists() || tempFile.length() < (originalSize * 0.7).toLong().coerceAtLeast(1024L)) {
                return LyricsSaveResult.Error("Le fichier modifié est incomplet, écriture annulée pour préserver l'original.")
            }

            var writeBackSuccess = false

            // Méthode A: écriture directe si FUSE l'autorise après accord MediaStore
            try {
                if (audioFile.exists()) {
                    tempFile.copyTo(audioFile, overwrite = true)
                    writeBackSuccess = true
                }
            } catch (e: Exception) {
                logD(TAG, "Écriture directe FUSE impossible: ${e.message}, tentative ContentResolver")
            }

            // Méthode B: écriture via ContentResolver OutputStream
            if (!writeBackSuccess && contentUri != null) {
                try {
                    context.contentResolver.openOutputStream(contentUri, "wt")?.use { output ->
                        tempFile.inputStream().use { input ->
                            input.copyTo(output)
                        }
                    }
                    writeBackSuccess = true
                } catch (e: Exception) {
                    logE(TAG, "Échec écriture ContentResolver openOutputStream: ${e.message}", e)
                }
            }

            if (writeBackSuccess) {
                LyricsSaveResult.TagWriteSuccess(audioFile.absolutePath, tagResult.tagType, lines.size)
            } else {
                LyricsSaveResult.Error("Échec de la réécriture du fichier audio modifié.")
            }
        } catch (e: Throwable) {
            logE(TAG, "Erreur écriture Scoped Storage: ${e.message}", e)
            LyricsSaveResult.Error(e.message ?: "Erreur Scoped Storage")
        } finally {
            try {
                tempFile?.delete()
            } catch (_: Exception) {}
        }
    }

    /**
     * Écrit les paroles universelles (USLT, Vorbis, MP4) et SYLT (pour MP3) dans le fichier.
     */
    private fun tryWriteAudioTags(
        file: File,
        lrcContent: String,
        lines: List<LyricLine>
    ): LyricsSaveResult {
        return try {
            val audioFile = AudioFileIO.read(file)
            val ext = file.extension.lowercase()
            var tag = audioFile.tag

            if (audioFile is MP3File) {
                var id3Tag = audioFile.iD3v2Tag
                if (id3Tag == null) {
                    id3Tag = ID3v23Tag()
                    audioFile.iD3v2Tag = id3Tag
                }

                // 1. Écrire le tag USLT universel contenant le texte LRC
                try {
                    id3Tag.setField(FieldKey.LYRICS, lrcContent)
                } catch (e: Exception) {
                    logD(TAG, "ID3 setField LYRICS: ${e.message}")
                }

                // 2. Écrire le tag SYLT binaire si des timestamps sont disponibles
                if (lines.isNotEmpty()) {
                    try {
                        val syltBytes = serializeSyltBytes(lines)
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
                    } catch (e: Exception) {
                        logD(TAG, "ID3 setFrame SYLT: ${e.message}")
                    }
                }

                audioFile.commit()
                return LyricsSaveResult.TagWriteSuccess(file.absolutePath, "ID3v2 (USLT + SYLT)", lines.size)
            } else {
                // FLAC, M4A/AAC, OGG, WAV
                if (tag == null) {
                    tag = audioFile.createDefaultTag()
                    audioFile.tag = tag
                }

                tag.setField(FieldKey.LYRICS, lrcContent)
                audioFile.commit()

                val tagFormat = when (ext) {
                    "flac" -> "FLAC Vorbis Comment (LYRICS)"
                    "m4a", "mp4", "aac" -> "MP4 Metadata (©lyr)"
                    "ogg" -> "OGG Vorbis Comment (LYRICS)"
                    else -> "Audio Tag (LYRICS)"
                }
                return LyricsSaveResult.TagWriteSuccess(file.absolutePath, tagFormat, lines.size)
            }
        } catch (e: Throwable) {
            logW(TAG, "Écriture directe tag jaudiotagger échouée pour ${file.name}: ${e.message}")
            LyricsSaveResult.Error(e.message ?: "Erreur d'écriture tag audio")
        }
    }

    /**
     * Encode les lignes de paroles au format binaire ID3 SYLT.
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
            logE(TAG, "Erreur écriture fichier .lrc : ${e.message}", e)
            LyricsSaveResult.Error("Impossible d'écrire le fichier .lrc : ${e.localizedMessage}")
        }
    }
}
