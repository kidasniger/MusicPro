package com.example.data.preferences

/** Échelle typographique globale appliquée au fontScale Compose. */
enum class TextSizePreference(val label: String, val multiplier: Float) {
    SMALL("Petit", 0.90f),
    NORMAL("Normal", 1.00f),
    LARGE("Grand", 1.12f),
    EXTRA_LARGE("Très grand", 1.25f)
}

/** Densité verticale des lignes de bibliothèque. */
enum class ListDensityPreference(val label: String, val rowHeightDp: Int) {
    COMFORTABLE("Confortable", 64),
    COMPACT("Compacte", 52)
}

fun resolveTextSizePreference(value: String?): TextSizePreference =
    TextSizePreference.entries.firstOrNull { it.name == value } ?: TextSizePreference.NORMAL

fun resolveListDensityPreference(value: String?): ListDensityPreference =
    ListDensityPreference.entries.firstOrNull { it.name == value } ?: ListDensityPreference.COMFORTABLE
