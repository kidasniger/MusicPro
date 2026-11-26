package com.example.util

/**
 * Mise à jour 101 — nettoyage d'affichage des titres de morceaux.
 *
 * MediaStore et certains gestionnaires de fichiers laissent parfois un compteur de doublon
 * en suffixe du titre (« LOCO Clip officiel (0) », « Démo [2] »). Le compteur reste présent
 * dans la bibliothèque : il est uniquement masqué à l'affichage pour une pilule titre plus
 * lisible. Les années (« Bella (2024) ») et les vraies mentions entre parenthèses
 * (« Intro (Live) », « (Remastered) ») sont conservées.
 */
object TrackTitleFormatter {

    /** Suffixe numérique court (1 à 3 chiffres) entre parenthèses ou crochets, en toute fin de titre. */
    private val duplicateSuffix = Regex("""\s*[\(\[]\d{1,3}[\)\]]\s*$""")

    /**
     * Renvoie le titre à afficher, sans compteur de doublon final. Ne renvoie jamais de
     * chaîne vide si [rawTitle] n'est pas vide : en dernier recours, le titre d'origine
     * est conservé tel quel.
     */
    fun displayTitle(rawTitle: String?): String {
        if (rawTitle.isNullOrBlank()) return ""
        val trimmed = rawTitle.trim()
        val cleaned = trimmed.replace(duplicateSuffix, "").trim()
        return cleaned.ifBlank { trimmed }
    }
}
