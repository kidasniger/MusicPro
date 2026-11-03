package com.example.data.preferences

/**
 * Mise à jour 055 — modes de tri de la bibliothèque.
 * Le dernier choix est mémorisé dans DataStore (clé `library_sort_mode`).
 */
enum class LibrarySortMode {
    /** A–Z (titre, insensible à la casse). */
    TITLE_AZ,

    /** Récents (date d'ajout décroissante). */
    RECENT,

    /** Durée (morceaux les plus longs d'abord). */
    DURATION,

    /** Plus écoutés (dernière lecture la plus récente d'abord). */
    MOST_PLAYED
}

/**
 * Résout la valeur brute stockée dans DataStore ; retombe sur A–Z si la clé
 * est absente ou corrompue.
 */
fun resolveLibrarySortMode(raw: String?): LibrarySortMode =
    LibrarySortMode.entries.firstOrNull { it.name == raw } ?: LibrarySortMode.TITLE_AZ
