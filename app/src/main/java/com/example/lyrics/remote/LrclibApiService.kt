package com.example.lyrics.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface LrclibApiService {

    /**
     * Recherche de paroles sur lrclib.net.
     * Endpoint : /api/search
     * Paramètres :
     * - track_name : Titre du morceau
     * - artist_name : Nom de l'artiste
     * - album_name : Nom de l'album
     * - duration : Durée en secondes
     * - q : Requête texte globale
     */
    @GET("api/search")
    suspend fun searchLyrics(
        @Query("track_name") trackName: String? = null,
        @Query("artist_name") artistName: String? = null,
        @Query("album_name") albumName: String? = null,
        @Query("duration") duration: Int? = null,
        @Query("q") query: String? = null
    ): List<LrclibSearchResult>
}
