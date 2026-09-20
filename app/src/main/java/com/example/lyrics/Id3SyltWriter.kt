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
import java.io.File
import java.io.IOException
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
        var originalBackup: File? = null
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

            originalBackup = File(
                context.cacheDir,
                "tag_original_" + System.currentTimeMillis() + ".tmp"
            )
            tempFile.copyTo(originalBackup, overwrite = true)

            val tagResult = tryWriteAudioTags(tempFile, lrcContent, lines)
            if (tagResult !is LyricsSaveResult.TagWriteSuccess) {
                return tagResult
            }

            val modifiedSize = tempFile.length()
            if (!tempFile.exists() || modifiedSize <= 0L) {
                return LyricsSaveResult.Error("Le fichier modifié est vide, écriture annulée pour préserver le fichier original.")
            }

            var writeBackSuccess = false

            // Méthode A: écriture directe si FUSE l'autorise après accord MediaStore
            try {
                if (audioFile.exists()) {
                    tempFile.copyTo(audioFile, overwrite = true)
                    writeBackSuccess =
                        audioFile.exists() &&
                        audioFile.length() == tempFile.length() &&
                        filesMatchExactly(tempFile, audioFile)
                }
            } catch (e: Exception) {
                logD(TAG, "Écriture directe FUSE impossible: ${e.message}, tentative ContentResolver")
            }

            // Méthode B: écriture via ContentResolver OutputStream
            if (!writeBackSuccess && contentUri != null) {
                try {
                    var bytesWritten = -1L
                    context.contentResolver.openOutputStream(contentUri, "rwt")?.use { output ->
                        tempFile.inputStream().use { input ->
                            bytesWritten = input.copyTo(output)
                        }
                        output.flush()
                    } ?: throw IOException("openOutputStream() a retourné null")

                    var verifiedBytes = -1L
                    context.contentResolver.openInputStream(contentUri)?.use { input ->
                        val buffer = ByteArray(16 * 1024)
                        var total = 0L
                        while (true) {
                            val read = input.read(buffer)
                            if (read <= 0) break
                            total += read
                            if (total > tempFile.length()) break
                        }
                        verifiedBytes = total
                    }

                    writeBackSuccess =
                        bytesWritten == tempFile.length() &&
                        verifiedBytes == tempFile.length() &&
                        contentUriMatchesFile(context, contentUri, tempFile)
                } catch (e: Exception) {
                    logE(TAG, "Échec écriture ContentResolver openOutputStream: ${e.message}", e)
                }
            }

            if (!writeBackSuccess && originalBackup != null && originalBackup.exists()) {
                try {
                    if (audioFile.exists() && audioFile.canWrite()) {
                        originalBackup.copyTo(audioFile, overwrite = true)
                    } else if (contentUri != null) {
                        context.contentResolver.openOutputStream(contentUri, "rwt")?.use { output ->
                            originalBackup.inputStream().use { input -> input.copyTo(output) }
                            output.flush()
                        } ?: throw IOException("Impossible de restaurer le fichier original.")
                    }
                } catch (restoreError: Exception) {
                    logE(TAG, "Échec restauration après écriture incomplète: " + restoreError.message, restoreError)
                }
            }

            if (writeBackSuccess) {
                LyricsSaveResult.TagWriteSuccess(audioFile.absolutePath, tagResult.tagType, lines.size)
            } else {
                LyricsSaveResult.Error("Échec de la réécriture vérifiée du fichier audio. Le fichier original est conservé.")
            }
        } catch (e: Throwable) {
            logE(TAG, "Erreur écriture Scoped Storage: ${e.message}", e)
            LyricsSaveResult.Error(e.message ?: "Erreur Scoped Storage")
        } finally {
            try {
                tempFile?.delete()
                originalBackup?.delete()
            } catch (_: Exception) {}
        }
    }

    private fun filesMatchExactly(first: File, second: File): Boolean {
        if (!first.isFile || !second.isFile || first.length() != second.length()) return false

        first.inputStream().use { left ->
            second.inputStream().use { right ->
                val leftBuffer = ByteArray(32 * 1024)
                val rightBuffer = ByteArray(32 * 1024)

                while (true) {
                    val leftRead = left.read(leftBuffer)
                    val rightRead = right.read(rightBuffer)

                    if (leftRead != rightRead) return false
                    if (leftRead <= 0) return true

                    for (index in 0 until leftRead) {
                        if (leftBuffer[index] != rightBuffer[index]) return false
                    }
                }
            }
        }
    }

    private fun contentUriMatchesFile(
        context: Context,
        contentUri: Uri,
        source: File
    ): Boolean {
        val resolver = context.contentResolver
        resolver.openInputStream(contentUri)?.use { input ->
            source.inputStream().use { sourceInput ->
                val targetBuffer = ByteArray(32 * 1024)
                val sourceBuffer = ByteArray(32 * 1024)

                while (true) {
                    val sourceRead = sourceInput.read(sourceBuffer)
                    val targetRead = input.read(targetBuffer)

                    if (sourceRead != targetRead) return false
                    if (sourceRead <= 0) return true

                    for (index in 0 until sourceRead) {
                        if (sourceBuffer[index] != targetBuffer[index]) return false
                    }
                }
            }
        } ?: return false
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
                                body = FrameBodySYLT(SyltCodec.TEXT_ENCODING_UTF16, "eng", 2, 1, "", syltBytes)
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
    private fun serializeSyltBytes(lines: List<LyricLine>): ByteArray =
        SyltCodec.serialize(lines)

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
            if (!dir.exists() && !dir.mkdirs()) {
                return LyricsSaveResult.Error("Impossible de créer le dossier de destination.")
            }
            if (!dir.isDirectory) {
                return LyricsSaveResult.Error("Le chemin de destination nest pas un dossier.")
            }
            val lrcFile = File(dir, "$fileName.lrc")
            val expectedSize = content.toByteArray(StandardCharsets.UTF_8).size.toLong()
            lrcFile.writeText(content, StandardCharsets.UTF_8)
            if (!lrcFile.isFile || lrcFile.length() != expectedSize) {
                return LyricsSaveResult.Error("Le fichier .lrc ne correspond pas aux données écrites.")
            }
            LyricsSaveResult.LrcFileSuccess(lrcFile.absolutePath, linesCount)
        } catch (e: Exception) {
            logE(TAG, "Erreur écriture fichier .lrc : ${e.message}", e)
            LyricsSaveResult.Error("Impossible d'écrire le fichier .lrc : ${e.localizedMessage}")
        }
    }
}
