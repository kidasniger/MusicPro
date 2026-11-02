package com.example.data.scanner

/** Artiste et titre lus dans un nom de fichier du type « Artiste - Titre ». */
internal data class FileNameMetadata(val artist: String, val title: String)

/** Un nom d'artiste plus long que cela est probablement une phrase, pas un artiste. */
internal const val MaxFileNameArtistLength = 60

private val Separators = listOf(" - ", " – ", " — ")
private val LeadingTrackNumber = Regex("^\\d{1,3}\\s*[-.)]\\s+")
private val Spaces = Regex("\\s+")

/**
 * Lit l'artiste et le titre dans un nom de fichier sans extension, par exemple
 * `La_Rvfleuze_-_Serrure_#1(0)` donne l'artiste « La Rvfleuze » et le titre « Serrure #1(0) ».
 *
 * Les tirets du bas deviennent des espaces et un numéro de piste initial (« 01 - ») est ignoré.
 * Renvoie `null` quand le nom ne suit pas ce modèle (par exemple `AUD-20260805-WA0012`).
 */
internal fun metadataFromFileName(nameWithoutExtension: String): FileNameMetadata? {
    val spaced = nameWithoutExtension.replace('_', ' ').replace(Spaces, " ").trim()
    val cleaned = spaced.replace(LeadingTrackNumber, "")
    val separator = Separators.firstOrNull { cleaned.contains(it) } ?: return null
    val artist = cleaned.substringBefore(separator).trim()
    val title = cleaned.substringAfter(separator).trim()
    if (artist.isBlank() || title.isBlank()) return null
    if (artist.length > MaxFileNameArtistLength) return null
    if (artist.all { it.isDigit() }) return null
    return FileNameMetadata(artist = artist, title = title)
}
