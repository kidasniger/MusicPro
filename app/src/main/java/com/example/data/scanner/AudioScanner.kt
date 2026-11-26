package com.example.data.scanner

import com.example.data.local.AudioTrackEntity

interface AudioScanner {
    suspend fun scanAudioFiles(): List<AudioTrackEntity>

    /**
     * Recrée les fichiers de jaquettes manquants du cache à partir de l'image
     * embarquée dans chaque fichier audio, sans modifier la base de données.
     * Retourne le nombre de fichiers recréés. Implémentation par défaut : aucune
     * (les faux scanners des tests n'ont pas de cache à réparer).
     */
    suspend fun repairMissingArtworkFiles(tracks: List<AudioTrackEntity>): Int = 0
}
