package com.example.ui.artist

import com.example.data.local.AudioTrackEntity

/** Nom affiché quand le fichier n'a pas d'artiste (identique à celui des résumés de la bibliothèque). */
internal const val UnknownArtistName = "Artiste Inconnu"

/** Au-delà de ce nombre de titres, une sortie est classée « Discographie » ; sinon « Singles et EP ». */
internal const val SingleMaxTracks = 3

/** Nombre de titres montrés avant « Voir plus ». */
internal const val TopTracksLimit = 5

/** Mise à jour 066 — maximum affiché après déploiement de « Voir plus ». */
internal const val TopTracksExpandedLimit = 10

/** Nombre maximum d'artistes similaires proposés. */
internal const val SimilarArtistsLimit = 10

internal fun normalizedArtist(artist: String): String = artist.ifBlank { UnknownArtistName }

/** Une sortie (album, single ou apparition) d'un artiste, regroupée à partir des fichiers détectés. */
internal data class ArtistRelease(
    val name: String,
    val coverUri: String?,
    val tracks: List<AudioTrackEntity>,
    /** Mise à jour 067 — année (fichiers ajoutés), null si inconnue. */
    val year: Int? = null
)

/** Un artiste proche, déduit des dossiers partagés (aucune donnée de genre n'est stockée). */
internal data class SimilarArtist(
    val name: String,
    val coverUri: String?,
    val trackCount: Int
)

/** Tous les morceaux d'un artiste, par titre. */
internal fun artistTracksOf(tracks: List<AudioTrackEntity>, artistName: String): List<AudioTrackEntity> =
    tracks.filter { normalizedArtist(it.artist) == artistName }
        .sortedBy { it.title.lowercase() }

/**
 * Mise à jour 067 — année d'une sortie : déduite de la date d'ajout locale du fichier
 * (aucune année de sortie n'est stockée dans les métadonnées). Null si absente.
 */
internal fun releaseYearOf(dateAddedSeconds: Long): Int? {
    if (dateAddedSeconds <= 0L) return null
    val calendar = java.util.Calendar.getInstance()
    calendar.timeInMillis = dateAddedSeconds * 1000L
    return calendar.get(java.util.Calendar.YEAR)
}

/**
 * Les sorties d'un artiste, par nom d'album, avec leur année (date d'ajout la plus récente).
 */
internal fun artistReleasesOf(artistTracks: List<AudioTrackEntity>): List<ArtistRelease> =
    artistTracks.groupBy { it.album.ifBlank { "Album Inconnu" } }
        .map { (album, list) ->
            ArtistRelease(
                name = album,
                coverUri = list.firstNotNullOfOrNull { it.albumArtUri?.takeIf(String::isNotBlank) },
                tracks = list.sortedBy { it.path.ifBlank { it.title } },
                year = list.maxOfOrNull { it.dateAdded }?.let(::releaseYearOf)
            )
        }
        .sortedBy { it.name.lowercase() }

internal fun discographyOf(releases: List<ArtistRelease>): List<ArtistRelease> =
    releases.filter { it.tracks.size > SingleMaxTracks }

internal fun singlesOf(releases: List<ArtistRelease>): List<ArtistRelease> =
    releases.filter { it.tracks.size <= SingleMaxTracks }

/**
 * Mise à jour 067 — « Apparitions » : albums où l'artiste apparaît mais qui appartiennent
 * d'abord à un autre artiste (celui qui y a le plus de morceaux).
 */
internal fun appearancesOf(
    allTracks: List<AudioTrackEntity>,
    artistName: String
): List<ArtistRelease> = allTracks
    .groupBy { it.album.ifBlank { "Album Inconnu" } }
    .mapNotNull { (album, list) ->
        val mine = list.filter { normalizedArtist(it.artist) == artistName }
        if (mine.isEmpty()) return@mapNotNull null
        val dominantArtist = list.groupingBy { normalizedArtist(it.artist) }.eachCount()
            .maxByOrNull { it.value }?.key
        if (dominantArtist == artistName) return@mapNotNull null
        ArtistRelease(
            name = album,
            coverUri = mine.firstNotNullOfOrNull { it.albumArtUri?.takeIf(String::isNotBlank) },
            tracks = mine.sortedBy { it.path.ifBlank { it.title } },
            year = list.maxOfOrNull { it.dateAdded }?.let(::releaseYearOf)
        )
    }
    .sortedBy { it.name.lowercase() }

/**
 * Mise à jour 066 — titres les plus joués : classement par nombre d'écoutes
 * ([playCounts], écoutes cumulées par morceau), puis par dernière écoute, puis par titre.
 * Sans historique ([playCounts] vide), l'ordre retombe sur les plus récents.
 */
internal fun topTracksOf(
    artistTracks: List<AudioTrackEntity>,
    limit: Int = TopTracksLimit,
    playCounts: Map<Long, Int> = emptyMap()
): List<AudioTrackEntity> =
    artistTracks
        .sortedWith(
            compareByDescending<AudioTrackEntity> { playCounts[it.id] ?: 0 }
                .thenByDescending { it.lastPlayed }
                .thenBy { it.title.lowercase() }
        )
        .take(limit)

/** Durée totale arrondie à la minute (1 minute au minimum si la liste n'est pas vide). */
internal fun artistDurationMinutes(artistTracks: List<AudioTrackEntity>): Int {
    if (artistTracks.isEmpty()) return 0
    return Math.round(artistTracks.sumOf { it.duration } / 60_000.0).toInt().coerceAtLeast(1)
}

/** Photo de l'artiste : la première pochette disponible de ses morceaux. */
internal fun artistCoverOf(artistTracks: List<AudioTrackEntity>): String? =
    artistTracks.firstNotNullOfOrNull { it.albumArtUri?.takeIf(String::isNotBlank) }

/**
 * Artistes proches : ceux dont des morceaux se trouvent dans les mêmes dossiers que ceux de
 * [artistName], classés par nombre de morceaux partagés puis par nom.
 */
internal fun similarArtistsOf(
    allTracks: List<AudioTrackEntity>,
    artistName: String,
    limit: Int = SimilarArtistsLimit
): List<SimilarArtist> {
    val folders = allTracks
        .filter { normalizedArtist(it.artist) == artistName }
        .map { it.folder }
        .filter { it.isNotBlank() }
        .toSet()
    if (folders.isEmpty()) return emptyList()
    return allTracks
        .filter { it.folder in folders && normalizedArtist(it.artist) != artistName }
        .groupBy { normalizedArtist(it.artist) }
        .map { (name, list) ->
            SimilarArtist(
                name = name,
                coverUri = artistCoverOf(list),
                trackCount = list.size
            )
        }
        .sortedWith(compareByDescending<SimilarArtist> { it.trackCount }.thenBy { it.name.lowercase() })
        .take(limit)
}
