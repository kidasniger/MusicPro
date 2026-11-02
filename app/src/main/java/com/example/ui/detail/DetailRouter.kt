package com.example.ui.detail

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Point d'entrée unique pour ouvrir un écran de détail (album, artiste) depuis n'importe quel écran
 * de l'accueil sans faire remonter de rappel dans `HomeScreen`. [DetailHost] lit la demande, l'affiche
 * puis la consomme.
 */
object DetailRouter {
    private val _request = MutableStateFlow<String?>(null)

    /** Clé de détail demandée (voir [DetailTarget]) ou `null` quand rien n'est en attente. */
    val request: StateFlow<String?> = _request.asStateFlow()

    fun open(key: String) {
        _request.value = key
    }

    fun openArtist(name: String) = open(DetailTarget.artistKey(name))

    fun openAlbum(name: String) = open(DetailTarget.albumKey(name))

    internal fun consume() {
        _request.value = null
    }
}
