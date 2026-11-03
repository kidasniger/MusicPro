package com.example.data.preferences

/**
 * Mise à jour 048 — Présentation du lecteur (Réglages › Lecture).
 *
 *  - [POCHETTE] : pochette carrée pleine largeur (défaut) ;
 *  - [VINYLE] : disque vinyle en rotation 33 tours, geste horizontal
 *    réservé au morceau précédent / suivant ;
 *  - [MINIMAL] : texte seul sur fond uni.
 */
enum class PlayerPresentation {
    POCHETTE,
    VINYLE,
    MINIMAL
}

/** Valeur stockée → énumération ; toute valeur inconnue ou absente revient à [PlayerPresentation.POCHETTE]. */
fun resolvePlayerPresentation(stored: String?): PlayerPresentation =
    PlayerPresentation.entries.firstOrNull { it.name == stored } ?: PlayerPresentation.POCHETTE
