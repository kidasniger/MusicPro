package com.example.ui.nowplaying

/**
 * Mise à jour 046 — Recherche du fichier vidéo associé à un morceau audio.
 *
 * Stratégie (du plus spécifique au plus large), dans le dossier du morceau :
 *  1. un fichier vidéo dont le nom de base est identique à celui du morceau (ex. « Concert.mp4 »
 *     pour « Concert.flac ») ;
 *  2. à défaut, n'importe quel fichier vidéo du dossier (clip du même album).
 */
object VideoMatcher {

    private val VIDEO_EXTENSIONS = listOf("mp4", "mkv", "webm", "avi", "mov", "m4v", "3gp")

    fun isVideoFile(fileName: String): Boolean {
        val lower = fileName.lowercase()
        return VIDEO_EXTENSIONS.any { lower.endsWith(".$it") }
    }

    /** Base du nom sans extension, comparaison insensible à la casse et aux accents simples. */
    private fun baseName(fileName: String): String =
        fileName.substringBeforeLast('.').trim().lowercase()

    /**
     * @param audioPath chemin complet du fichier audio (peut être vide/inconnu) ;
     * @param siblings noms des fichiers du même dossier.
     * @return le nom du fichier vidéo retenu, ou `null` s'il n'y en a pas.
     */
    fun findVideoFile(audioPath: String?, siblings: List<String>): String? {
        if (audioPath.isNullOrBlank()) return null
        val audioBase = baseName(audioPath.substringAfterLast('/'))
        if (audioBase.isEmpty()) return null
        // 1. Même nom de base.
        siblings.firstOrNull { sibling ->
            isVideoFile(sibling) && baseName(sibling) == audioBase
        }?.let { return it }
        // 2. N'importe quelle vidéo du dossier.
        return siblings.firstOrNull { isVideoFile(it) }
    }
}
