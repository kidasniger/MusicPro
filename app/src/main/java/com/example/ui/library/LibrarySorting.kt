package com.example.ui.library

import com.example.data.local.AudioTrackEntity
import com.example.data.preferences.LibrarySortMode
import com.example.ui.audio.AlbumSummary
import com.example.ui.audio.ArtistSummary

/**
 * Mise à jour 055 — tri de la bibliothèque (A–Z, Récents, Durée, Plus écoutés).
 * Fonctions pures, testées sans instrumentation Android.
 */
object LibrarySorting {

    fun sortTracks(tracks: List<AudioTrackEntity>, mode: LibrarySortMode): List<AudioTrackEntity> =
        when (mode) {
            LibrarySortMode.TITLE_AZ -> tracks.sortedBy { it.title.lowercase() }
            LibrarySortMode.RECENT -> tracks.sortedByDescending { it.dateAdded }
            LibrarySortMode.DURATION -> tracks.sortedByDescending { it.duration }
            LibrarySortMode.MOST_PLAYED -> tracks.sortedByDescending { it.lastPlayed }
        }

    /**
     * Pour les albums et les artistes, seuls A–Z et « plus écoutés »/récents ont
     * un sens : Durée retombe sur l'ordre alphabétique, Récents trie par la date
     * d'ajout du morceau échantillon, Plus écoutés par sa dernière lecture.
     */
    fun sortAlbums(albums: List<AlbumSummary>, mode: LibrarySortMode): List<AlbumSummary> =
        when (mode) {
            LibrarySortMode.TITLE_AZ, LibrarySortMode.DURATION ->
                albums.sortedBy { it.name.lowercase() }
            LibrarySortMode.RECENT ->
                albums.sortedByDescending { it.sampleTrack.dateAdded }
            LibrarySortMode.MOST_PLAYED ->
                albums.sortedByDescending { it.sampleTrack.lastPlayed }
        }

    fun sortArtists(artists: List<ArtistSummary>, mode: LibrarySortMode): List<ArtistSummary> =
        when (mode) {
            LibrarySortMode.TITLE_AZ, LibrarySortMode.DURATION ->
                artists.sortedBy { it.name.lowercase() }
            LibrarySortMode.RECENT -> artists // pas de date fiable par artiste
            LibrarySortMode.MOST_PLAYED -> artists
        }
}
