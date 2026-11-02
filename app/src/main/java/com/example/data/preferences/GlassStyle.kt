package com.example.data.preferences

/**
 * Curseurs « Film » et « Onglet actif » du verre (mise à jour 009).
 *
 *  - Film : opacité du voile de couleur du verre, de 0 à 100 %. 50 % (défaut) laisse le film de la
 *    recette inchangé ; 0 % le rend transparent ; 100 % le double (borné à 1). Le film de chaque
 *    niveau d'effet (« Équilibré » plus dense, aplat de « Le plus rapide ») est donc conservé au
 *    réglage par défaut.
 *  - Onglet actif : opacité de la capsule de l'onglet sélectionné de la barre de navigation,
 *    de 0 à 100 %. 26 % par défaut.
 */

/** Film par défaut, en pourcentage. */
const val DefaultGlassFilmOpacity = 50

/** Opacité par défaut de la capsule de l'onglet actif, en pourcentage. */
const val DefaultGlassActiveTabOpacity = 26

/** Bornes des deux curseurs, en pourcentage. */
const val MinGlassOpacity = 0
const val MaxGlassOpacity = 100

/** Réglage du curseur Film qui laisse le film de la recette tel quel. */
const val GlassFilmNeutralPercent = 50

/** Sous ce film (en %), le texte posé sur le verre peut devenir difficile à lire : un avertissement s'affiche. */
const val GlassFilmLowContrastPercent = 20

/** Ramène une opacité dans l'intervalle 0 à 100 %. */
fun clampGlassOpacity(percent: Int): Int = percent.coerceIn(MinGlassOpacity, MaxGlassOpacity)

/** Lit le film enregistré ; 50 % si rien n'est enregistré, valeur bornée à 0-100 %. */
fun resolveGlassFilmOpacity(stored: Int?): Int =
    clampGlassOpacity(stored ?: DefaultGlassFilmOpacity)

/** Lit l'opacité enregistrée de l'onglet actif ; 26 % par défaut, valeur bornée à 0-100 %. */
fun resolveGlassActiveTabOpacity(stored: Int?): Int =
    clampGlassOpacity(stored ?: DefaultGlassActiveTabOpacity)

/** Facteur appliqué au film de la recette : 0 à 50 % donne 0 à 1, 100 % donne 2. */
fun glassFilmScale(percent: Int): Float =
    clampGlassOpacity(percent) / GlassFilmNeutralPercent.toFloat()

/** Opacité finale du film : film de la recette x facteur du curseur, bornée à 0-1. */
fun glassScaledFilmAlpha(baseAlpha: Float, percent: Int): Float =
    (baseAlpha * glassFilmScale(percent)).coerceIn(0f, 1f)
