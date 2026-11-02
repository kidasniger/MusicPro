package com.example.data.local

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "listening_stats",
    primaryKeys = ["dayEpoch", "trackId"],
    indices = [Index(value = ["dayEpoch"])]
)
data class ListeningStatEntity(
    val dayEpoch: Long,
    val trackId: Long,
    val title: String,
    val artist: String,
    val genre: String,
    val listenedMs: Long
)
