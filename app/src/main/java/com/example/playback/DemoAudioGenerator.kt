package com.example.playback

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin

/**
 * Générateur d'audio PCM/WAV autonome pour les pistes de démonstration.
 *
 * Résout définitivement les erreurs FileNotFoundException / Source error dans Media3/ExoPlayer
 * sur émulateur ou appareil neuf sans fichiers musicaux préexistants, en fournissant de véritables
 * fichiers audio WAV audibles et conformes aux spécifications RIFF PCM.
 */
object DemoAudioGenerator {

    private const val TAG = "DemoAudioGenerator"
    private const val SAMPLE_RATE = 22050 // 22.05 kHz pour un bon compromis taille/clarté

    /**
     * Récupère ou génère un fichier audio WAV valide pour une piste de démonstration donnée.
     */
    fun getOrCreateDemoAudioFile(context: Context, trackId: Long, durationSeconds: Int = 18): File {
        val demoDir = File(context.filesDir, "demo_tracks").apply {
            if (!exists()) {
                mkdirs()
            }
        }
        val file = File(demoDir, "demo_track_$trackId.wav")

        // Si le fichier existe déjà et fait plus que la taille de l'en-tête, on le réutilise
        if (file.exists() && file.length() > 4096L) {
            return file
        }

        try {
            generateMusicalWav(file, trackId, durationSeconds)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la génération du fichier WAV démo pour la piste $trackId: ${e.message}", e)
        }

        return file
    }

    /**
     * Génère un fichier WAV PCM 16-bit mono contenant un arpège/mélodie synthétique unique.
     */
    private fun generateMusicalWav(file: File, trackId: Long, durationSeconds: Int) {
        val totalSamples = SAMPLE_RATE * durationSeconds
        val dataSize = totalSamples * 2 // 16 bits = 2 octets par échantillon mono

        FileOutputStream(file).use { out ->
            // 1. Écriture de l'en-tête RIFF WAV standard (44 octets)
            val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
            header.put("RIFF".toByteArray())
            header.putInt(36 + dataSize) // Taille restante
            header.put("WAVE".toByteArray())
            header.put("fmt ".toByteArray())
            header.putInt(16) // Taille de subchunk1 (16 pour PCM standard)
            header.putShort(1) // AudioFormat (1 = PCM non compressé)
            header.putShort(1) // NumChannels (1 = Mono)
            header.putInt(SAMPLE_RATE) // Fréquence d'échantillonnage
            header.putInt(SAMPLE_RATE * 2) // Débit en octets (SampleRate * NumChannels * 2)
            header.putShort(2) // BlockAlign (NumChannels * BitsPerSample / 8)
            header.putShort(16) // BitsPerSample
            header.put("data".toByteArray())
            header.putInt(dataSize) // Subchunk2Size

            out.write(header.array())

            // 2. Sélection d'une gamme musicale selon le trackId
            val chordNotes = when ((trackId % 6L).toInt()) {
                0 -> doubleArrayOf(220.0, 261.63, 329.63, 392.0, 440.0) // La mineur (Synthwave)
                1 -> doubleArrayOf(261.63, 329.63, 392.0, 523.25, 659.25) // Do majeur (Pop lumineux)
                2 -> doubleArrayOf(146.83, 220.0, 293.66, 369.99, 440.0) // Ré mineur (Dance rétro)
                3 -> doubleArrayOf(174.61, 261.63, 349.23, 440.0, 523.25) // Fa majeur (Rétro dreams)
                4 -> doubleArrayOf(196.0, 246.94, 293.66, 392.0, 493.88) // Sol majeur (Cosmic)
                else -> doubleArrayOf(164.81, 220.0, 246.94, 329.63, 392.0) // Mi mineur (Cyberpunk)
            }

            val noteDurationSamples = (SAMPLE_RATE * 0.28).toInt() // ~280ms par note
            val buffer = ByteArray(4096)
            var bufferPos = 0

            for (i in 0 until totalSamples) {
                val noteIdx = (i / noteDurationSamples) % chordNotes.size
                val freq = chordNotes[noteIdx]
                val t = i.toDouble() / SAMPLE_RATE

                // Enveloppe sonore ADSR simplifiée pour éviter les clics
                val posInNote = (i % noteDurationSamples).toDouble() / noteDurationSamples
                val envelope = when {
                    posInNote < 0.08 -> posInNote / 0.08 // Attaque douce
                    posInNote > 0.85 -> (1.0 - posInNote) / 0.15 // Décroissance douce
                    else -> 1.0 - (posInNote - 0.08) * 0.25 // Maintien
                }

                // Synthèse sonore : harmonique fondamentale + 2e et 3e harmoniques riches
                val wave = (sin(2.0 * PI * freq * t) * 0.60 +
                        sin(4.0 * PI * freq * t) * 0.25 +
                        sin(6.0 * PI * freq * t) * 0.15) * envelope

                val shortSample = (wave * 18000.0).toInt().coerceIn(-32768, 32767).toShort()

                // Encodage Little-Endian
                buffer[bufferPos++] = (shortSample.toInt() and 0xFF).toByte()
                buffer[bufferPos++] = ((shortSample.toInt() shr 8) and 0xFF).toByte()

                if (bufferPos >= buffer.size) {
                    out.write(buffer, 0, bufferPos)
                    bufferPos = 0
                }
            }

            if (bufferPos > 0) {
                out.write(buffer, 0, bufferPos)
            }
        }
    }
}
