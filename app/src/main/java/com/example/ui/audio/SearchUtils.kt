package com.example.ui.audio

import com.example.data.local.AudioTrackEntity

/**
 * Mise à jour 054 — utilitaires purs de la recherche locale :
 * historique (limite 20 entrées, dédoublonnage insensible à la casse),
 * décennies tapées comme une requête, et meilleure correspondance
 * pour la section « Meilleur résultat » (mise à jour 053).
 */
object SearchUtils {

    /**
     * Fusionne une nouvelle requête dans l'historique :
     * - la requête passe en tête de liste ;
     * - les doublons insensibles à la casse sont retirés ;
     * - l'historique est limité à [limit] entrées (20 par défaut).
     * Une requête vide ou blanche est ignorée.
     */
    fun mergeSearchHistory(
        existing: List<String>,
        newQuery: String,
        limit: Int = 20
    ): List<String> {
        val value = newQuery.trim()
        if (value.isBlank()) return existing.take(limit)
        val merged = mutableListOf(value)
        existing.forEach { entry ->
            if (!entry.equals(value, ignoreCase = true)) merged.add(entry)
        }
        return merged.take(limit)
    }

    /**
     * Retire une entrée de l'historique (insensible à la casse pour fiabiliser la puce ×).
     */
    fun removeEntry(history: List<String>, entry: String): List<String> =
        history.filterNot { it.equals(entry, ignoreCase = true) }

    private val DECADE_REGEX = Regex("""ann[éeèÉÈ]es?\s*(\d{2,4})""", RegexOption.IGNORE_CASE)

    /**
     * Mise à jour 054 — une puce « Années 1990 » tape la requête du même nom.
     * Analyse une requête de décennie et renvoie la plage d'années couverte
     * (1990..1999 pour « Années 1990 »), ou null si la requête n'est pas une décennie.
     */
    fun decadeRangeForQuery(query: String): IntRange? {
        val match = DECADE_REGEX.find(query.trim()) ?: return null
        var year = match.groupValues[1].toIntOrNull() ?: return null
        // « 90 » désigne les années 1990, « 200 » n'est pas une décennie valide.
        if (year < 100) year += if (year < 60) 2000 else 1900
        val start = year - year.mod(10)
        return start..(start + 9)
    }

    /**
     * Mise à jour 053 — meilleur résultat d'une recherche :
     * priorité aux titres commençant par la requête, puis aux titres la
     * contenant plus tôt, puis à l'ordre naturel de la liste.
     */
    fun bestMatchIndex(tracks: List<AudioTrackEntity>, query: String): Int {
        if (query.isBlank() || tracks.isEmpty()) return -1
        val q = query.trim().lowercase()
        var startsWith = -1
        var containsEarly = -1
        tracks.forEachIndexed { index, track ->
            val title = track.title.lowercase()
            if (startsWith == -1 && title.startsWith(q)) startsWith = index
            if (containsEarly == -1 && title.contains(q)) containsEarly = index
        }
        return when {
            startsWith != -1 -> startsWith
            containsEarly != -1 -> containsEarly
            else -> 0
        }
    }

    /**
     * Mise à jour 056 — première lettre d'un libellé pour l'index A–Z ;
     * tout caractère non alphabétique est rangé sous « # ».
     */
    fun firstLetterOf(text: String): Char {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return '#'
        val first = trimmed[0].uppercaseChar()
        return if (first in 'A'..'Z') first else '#'
    }

    /**
     * Mise à jour 056 — lettres présentes dans la liste, dans l'ordre A–Z
     * avec « # » en fin, sans doublon.
     */
    fun lettersFor(labels: List<String>): List<Char> {
        val letters = labels.map { firstLetterOf(it) }.distinct().toMutableList()
        val sorted = letters.filter { it != '#' }.sorted()
        return if (letters.contains('#')) sorted + '#' else sorted
    }
}
