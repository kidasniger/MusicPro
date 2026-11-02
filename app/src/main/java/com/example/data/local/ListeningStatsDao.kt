package com.example.data.local

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ListeningStatsDao {
    @Query(
        """
        INSERT INTO listening_stats(dayEpoch, trackId, title, artist, genre, listenedMs)
        VALUES(:dayEpoch, :trackId, :title, :artist, :genre, :listenedMs)
        ON CONFLICT(dayEpoch, trackId) DO UPDATE SET
            title = excluded.title,
            artist = excluded.artist,
            genre = excluded.genre,
            listenedMs = listening_stats.listenedMs + excluded.listenedMs
        """
    )
    suspend fun addListeningTime(
        dayEpoch: Long,
        trackId: Long,
        title: String,
        artist: String,
        genre: String,
        listenedMs: Long
    )

    @Query("SELECT * FROM listening_stats ORDER BY dayEpoch DESC")
    fun observeAll(): Flow<List<ListeningStatEntity>>
}
