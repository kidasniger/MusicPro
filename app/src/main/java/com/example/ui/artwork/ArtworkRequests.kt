package com.example.ui.artwork

import android.content.Context
import coil.Coil
import coil.request.ImageRequest

/** Taille (en px) des miniatures de grilles (mise à jour 087). */
const val ArtworkGridSizePx = 300

/** Taille (en px) des pochettes du lecteur plein écran (mise à jour 087). */
const val ArtworkPlayerSizePx = 1080

/** Fondu enchaîné à l'apparition d'une pochette, en ms (mise à jour 087). */
const val ArtworkCrossfadeMs = 200

/**
 * Requête d'image optimisée pour les grilles (mise à jour 087) : le décodeur est limité à
 * [ArtworkGridSizePx] px — une miniature est bien plus légère que la pochette pleine taille.
 */
fun Context.artworkGridRequest(model: Any?): ImageRequest =
    ImageRequest.Builder(this)
        .data(model)
        .size(ArtworkGridSizePx)
        .crossfade(ArtworkCrossfadeMs)
        .build()

/**
 * Requête d'image pour le lecteur (mise à jour 087) : jusqu'à [ArtworkPlayerSizePx] px pour
 * rester nette en plein écran.
 */
fun Context.artworkPlayerRequest(model: Any?): ImageRequest =
    ImageRequest.Builder(this)
        .data(model)
        .size(ArtworkPlayerSizePx)
        .crossfade(ArtworkCrossfadeMs)
        .build()

/**
 * Précharge la pochette dans les caches mémoire + disque (mise à jour 087) : appelé quand le
 * morceau change pour que la pochette du morceau suivant soit déjà prête.
 */
fun Context.preloadArtwork(model: Any?) {
    if (model == null) return
    val request = ImageRequest.Builder(this)
        .data(model)
        .size(ArtworkPlayerSizePx)
        .build()
    Coil.imageLoader(this).enqueue(request)
}
