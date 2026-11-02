package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ListeningStatsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfMissing(stat: ListeningStatEntity)

    @Query(
        """
        UPDATE listening_stats
        SET title = :title,
            artist = :artist,
            genre = :genre,
            listenedMs = listenedMs + :listenedMs
        WHERE dayEpoch = :dayEpoch AND trackId = :trackId
        """
    )
    suspend fun incrementListeningTime(
        dayEpoch: Long,
        trackId: Long,
        title: String,
        artist: String,
        genre: String,
        listenedMs: Long
    )

    @Transaction
    suspend fun addListeningTime(
        dayEpoch: Long,
        trackId: Long,
        title: String,
        artist: String,
        genre: String,
        listenedMs: Long
    ) {
        if (listenedMs <= 0L) return
        insertIfMissing(
            ListeningStatEntity(
                dayEpoch = dayEpoch,
                trackId = trackId,
                title = title,
                artist = artist,
                genre = genre,
                listenedMs = 0L
            )
        )
        incrementListeningTime(dayEpoch, trackId, title, artist, genre, listenedMs)
    }

    @Query("SELECT * FROM listening_stats ORDER BY dayEpoch DESC")
    fun observeAll(): Flow<List<ListeningStatEntity>>
}
