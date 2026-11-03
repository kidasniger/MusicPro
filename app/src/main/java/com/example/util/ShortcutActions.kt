package com.example.util

/**
 * Mise à jour 080 — actions des raccourcis d'application (appui long sur l'icône).
 *
 * « Lecture aléatoire » et « Rechercher » ouvrent [MainActivity] avec l'action
 * correspondante ; « Dernière lecture » réutilise l'action existante du service de
 * lecture ([com.example.playback.MusicPlaybackService.ACTION_SHOW_NOW_PLAYING]).
 *
 * Objet pur (aucune dépendance Android) pour rester testable sans Robolectric.
 */
object ShortcutActions {

    const val ACTION_SHUFFLE_PLAY = "com.example.musicpro.action.SHUFFLE_PLAY"
    const val ACTION_OPEN_SEARCH = "com.example.musicpro.action.OPEN_SEARCH"

    /** Requête déduite d'une action d'intent de raccourci. */
    enum class ShortcutRequest { SHUFFLE_PLAY, OPEN_SEARCH, OPEN_LAST_PLAYED }

    /**
     * Analyse l'action d'un intent. Renvoie `null` pour toute action non reconnue
     * (lancement normal, mise à jour, notification…).
     */
    fun parse(action: String?): ShortcutRequest? = when (action) {
        ACTION_SHUFFLE_PLAY -> ShortcutRequest.SHUFFLE_PLAY
        ACTION_OPEN_SEARCH -> ShortcutRequest.OPEN_SEARCH
        "com.example.musicpro.ACTION_SHOW_NOW_PLAYING" -> ShortcutRequest.OPEN_LAST_PLAYED
        else -> null
    }
}
