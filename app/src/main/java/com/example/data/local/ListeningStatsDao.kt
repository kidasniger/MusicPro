package com.example.data.local

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ListeningStatsDao {
    @Query(
        """
        INSERT OR REPLACE INTO listening_stats(
            dayEpoch, trackId, title, artist, genre, listenedMs
        )
        VALUES(
            :dayEpoch,
            :trackId,
            :title,
            :artist,
            :genre,
            COALESCE(
                (SELECT listenedMs FROM listening_stats
                 WHERE dayEpoch = :dayEpoch AND trackId = :trackId),
                0
            ) + :listenedMs
        )
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
