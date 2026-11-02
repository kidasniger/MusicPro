package com.example.data.preferences

/**
 * Réglages d'apparence indépendants (mise à jour 013) : « Apparence » × « Style » = 12 combinaisons
 * avec « Sombre le soir » (mise à jour 018). Fonctions pures, sans Android, donc testables.
 */

/** Apparence : clair, sombre, suit le système, ou sombre sur une plage horaire. */
enum class AppearanceMode { LIGHT, DARK, AUTO, DARK_EVENING }

/** Style visuel de l'application. */
enum class AppStyle { MINIMAL, NEON, GLASS }

/** Couleur d'accent (styles Minimal et Verre). ARTWORK = couleur dominante de la pochette. */
enum class AccentChoice { TERRACOTTA, BLUE, GREEN, VIOLET, ARTWORK }

const val MinutesPerDay = 24 * 60

/** Début par défaut de « Sombre le soir » : 20:00. */
const val DefaultEveningStartMinutes = 20 * 60

/** Fin par défaut de « Sombre le soir » : 07:00. */
const val DefaultEveningEndMinutes = 7 * 60

/** Ramène une heure exprimée en minutes depuis minuit dans 0..1439. */
fun clampDayMinutes(minutes: Int): Int = minutes.coerceIn(0, MinutesPerDay - 1)

/**
 * Migration de l'ancienne clé `app_theme_mode` :
 * DARK → (Sombre, Néon), LIGHT → (Clair, Minimal), SYSTEM → (Auto, Minimal).
 * Sans ancienne valeur, on garde le comportement historique : Sombre + Néon.
 */
fun legacyThemeToAppearance(legacy: String?): AppearanceMode = when (legacy) {
    "LIGHT" -> AppearanceMode.LIGHT
    "SYSTEM" -> AppearanceMode.AUTO
    else -> AppearanceMode.DARK
}

fun legacyThemeToStyle(legacy: String?): AppStyle = when (legacy) {
    "LIGHT", "SYSTEM" -> AppStyle.MINIMAL
    else -> AppStyle.NEON
}

/** Valeur enregistrée si elle est valide, sinon déduite de l'ancienne clé. */
fun resolveAppearanceMode(stored: String?, legacy: String?): AppearanceMode =
    AppearanceMode.values().firstOrNull { it.name == stored } ?: legacyThemeToAppearance(legacy)

fun resolveAppStyle(stored: String?, legacy: String?): AppStyle =
    AppStyle.values().firstOrNull { it.name == stored } ?: legacyThemeToStyle(legacy)

fun resolveAccentChoice(stored: String?): AccentChoice =
    AccentChoice.values().firstOrNull { it.name == stored } ?: AccentChoice.TERRACOTTA

fun resolveEveningMinutes(stored: Int?, default: Int): Int = clampDayMinutes(stored ?: default)

/**
 * Vrai si [nowMinutes] tombe dans la plage sombre [startMinutes] → [endMinutes] (fin exclue).
 * La plage peut passer minuit (20:00 → 07:00). Début = fin : plage vide.
 */
fun isInDarkWindow(nowMinutes: Int, startMinutes: Int, endMinutes: Int): Boolean {
    if (startMinutes == endMinutes) return false
    return if (startMinutes < endMinutes) {
        nowMinutes >= startMinutes && nowMinutes < endMinutes
    } else {
        nowMinutes >= startMinutes || nowMinutes < endMinutes
    }
}
