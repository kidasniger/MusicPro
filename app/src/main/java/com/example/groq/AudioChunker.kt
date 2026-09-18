package com.example.groq

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class AudioChunk(
    val file: File,
    val timeOffsetSeconds: Double,
    val isTempFile: Boolean = false
)

object AudioChunker {

    private const val TAG = "AudioChunker"

    // Limite de taille directe pour Groq (25 Mo). Nous fixons le seuil sécurisé à 22 Mo.
    const val MAX_GROQ_FILE_BYTES: Long = 22 * 1024 * 1024L // 22 MB

    /**
     * Découpe le fichier audio en morceaux compatibles (< 22 Mo) si nécessaire,
     * en calculant le décalage temporel (timeOffsetSeconds) pour chaque morceau.
     */
    fun prepareChunks(
        context: Context,
        audioFile: File,
        totalDurationMs: Long = 0L
    ): List<AudioChunk> {
        if (!audioFile.exists() || audioFile.length() == 0L) {
            return emptyList()
        }

        // Si le fichier est déjà inférieur à la limite Groq, aucun découpage nécessaire
        if (audioFile.length() <= MAX_GROQ_FILE_BYTES) {
            return listOf(AudioChunk(file = audioFile, timeOffsetSeconds = 0.0, isTempFile = false))
        }

        Log.d(TAG, "Fichier volumineux détecté (${audioFile.length() / (1024 * 1024)} Mo) > seuil 22 Mo. Découpage en cours...")

        val ext = audioFile.extension.lowercase()
        return try {
            when (ext) {
                "wav" -> splitWavFile(context, audioFile)
                "mp3" -> splitMp3File(context, audioFile, totalDurationMs)
                else -> splitGenericAudioFile(context, audioFile, totalDurationMs)
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Erreur lors du découpage de l'audio : ${e.message}, tentative avec le fichier complet.", e)
            listOf(AudioChunk(file = audioFile, timeOffsetSeconds = 0.0, isTempFile = false))
        }
    }

    /**
     * Découpage sécurisé pour fichiers WAV (maintien du header standard 44 octets).
     */
    private fun splitWavFile(context: Context, wavFile: File): List<AudioChunk> {
        val chunks = mutableListOf<AudioChunk>()
        val totalLength = wavFile.length()
        if (totalLength <= 44) {
            return listOf(AudioChunk(wavFile, 0.0, false))
        }

        val fis = FileInputStream(wavFile)
        val header = ByteArray(44)
        val readHeader = fis.read(header)
        if (readHeader < 44) {
            fis.close()
            return listOf(AudioChunk(wavFile, 0.0, false))
        }

        val buffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)
        val byteRate = buffer.getInt(28).coerceAtLeast(1) // Octets par seconde

        val targetDataChunkSize = (18 * 1024 * 1024).toInt() // 18 Mo par chunk de données
        val dataBuffer = ByteArray(64 * 1024)
        var totalDataBytesProcessed = 0L
        var chunkIndex = 0

        while (fis.available() > 0) {
            val chunkFile = File(context.cacheDir, "groq_chunk_${chunkIndex}_${wavFile.name}")
            val fos = FileOutputStream(chunkFile)

            // Écrire un header factice initial de 44 octets
            fos.write(header)
            var currentChunkDataBytes = 0

            while (currentChunkDataBytes < targetDataChunkSize && fis.available() > 0) {
                val toRead = minOf(dataBuffer.size, targetDataChunkSize - currentChunkDataBytes)
                val read = fis.read(dataBuffer, 0, toRead)
                if (read <= 0) break
                fos.write(dataBuffer, 0, read)
                currentChunkDataBytes += read
            }
            fos.flush()
            fos.close()

            // Mettre à jour la taille dans l'en-tête WAV du chunk
            val chunkHeader = header.copyOf()
            val chunkBuf = ByteBuffer.wrap(chunkHeader).order(ByteOrder.LITTLE_ENDIAN)
            chunkBuf.putInt(4, currentChunkDataBytes + 36) // RIFF ChunkSize
            chunkBuf.putInt(40, currentChunkDataBytes) // Subchunk2Size (data size)

            val patchRaf = java.io.RandomAccessFile(chunkFile, "rw")
            patchRaf.seek(0)
            patchRaf.write(chunkHeader)
            patchRaf.close()

            val timeOffsetSec = totalDataBytesProcessed.toDouble() / byteRate
            chunks.add(AudioChunk(chunkFile, timeOffsetSec, isTempFile = true))

            totalDataBytesProcessed += currentChunkDataBytes
            chunkIndex++
        }

        fis.close()
        return if (chunks.isNotEmpty()) chunks else listOf(AudioChunk(wavFile, 0.0, false))
    }

    /**
     * Découpage pour fichiers MP3 le long des frontières de trames.
     */
    private fun splitMp3File(context: Context, mp3File: File, totalDurationMs: Long): List<AudioChunk> {
        val chunks = mutableListOf<AudioChunk>()
        val totalLength = mp3File.length()
        val targetChunkSize = 18 * 1024 * 1024L // 18 Mo

        val fis = FileInputStream(mp3File)
        val buffer = ByteArray(64 * 1024)
        var totalBytesRead = 0L
        var chunkIndex = 0

        val durationSec = if (totalDurationMs > 0) totalDurationMs / 1000.0 else 0.0

        while (fis.available() > 0) {
            val chunkFile = File(context.cacheDir, "groq_chunk_${chunkIndex}_${mp3File.name}")
            val fos = FileOutputStream(chunkFile)
            var currentChunkBytes = 0L

            while (currentChunkBytes < targetChunkSize && fis.available() > 0) {
                val toRead = minOf(buffer.size.toLong(), targetChunkSize - currentChunkBytes).toInt()
                val read = fis.read(buffer, 0, toRead)
                if (read <= 0) break
                fos.write(buffer, 0, read)
                currentChunkBytes += read
            }
            fos.flush()
            fos.close()

            val timeOffsetSec = if (totalLength > 0 && durationSec > 0) {
                (totalBytesRead.toDouble() / totalLength) * durationSec
            } else {
                chunkIndex * 600.0 // Estimation si durée inconnue
            }

            chunks.add(AudioChunk(chunkFile, timeOffsetSec, isTempFile = true))
            totalBytesRead += currentChunkBytes
            chunkIndex++
        }
        fis.close()

        return if (chunks.isNotEmpty()) chunks else listOf(AudioChunk(mp3File, 0.0, false))
    }

    /**
     * Découpage générique pour tout autre format audio.
     */
    private fun splitGenericAudioFile(context: Context, audioFile: File, totalDurationMs: Long): List<AudioChunk> {
        val chunks = mutableListOf<AudioChunk>()
        val totalLength = audioFile.length()
        val targetChunkSize = 18 * 1024 * 1024L // 18 Mo

        val fis = FileInputStream(audioFile)
        val buffer = ByteArray(64 * 1024)
        var totalBytesRead = 0L
        var chunkIndex = 0

        val durationSec = if (totalDurationMs > 0) totalDurationMs / 1000.0 else 0.0

        while (fis.available() > 0) {
            val chunkFile = File(context.cacheDir, "groq_chunk_${chunkIndex}_${audioFile.name}")
            val fos = FileOutputStream(chunkFile)
            var currentChunkBytes = 0L

            while (currentChunkBytes < targetChunkSize && fis.available() > 0) {
                val toRead = minOf(buffer.size.toLong(), targetChunkSize - currentChunkBytes).toInt()
                val read = fis.read(buffer, 0, toRead)
                if (read <= 0) break
                fos.write(buffer, 0, read)
                currentChunkBytes += read
            }
            fos.flush()
            fos.close()

            val timeOffsetSec = if (totalLength > 0 && durationSec > 0) {
                (totalBytesRead.toDouble() / totalLength) * durationSec
            } else {
                0.0
            }

            chunks.add(AudioChunk(chunkFile, timeOffsetSec, isTempFile = true))
            totalBytesRead += currentChunkBytes
            chunkIndex++
        }
        fis.close()

        return if (chunks.isNotEmpty()) chunks else listOf(AudioChunk(audioFile, 0.0, false))
    }

    /**
     * Nettoie les fichiers temporaires créés lors du découpage.
     */
    fun cleanupChunks(chunks: List<AudioChunk>) {
        for (chunk in chunks) {
            if (chunk.isTempFile) {
                try {
                    chunk.file.delete()
                } catch (e: Exception) {
                    Log.w(TAG, "Impossible de supprimer le fichier temporaire ${chunk.file.name}: ${e.message}")
                }
            }
        }
    }
}
