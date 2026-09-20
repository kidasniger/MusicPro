package com.example.data.scanner

import com.example.data.local.AudioTrackEntity

interface AudioScanner {
    suspend fun scanAudioFiles(): List<AudioTrackEntity>
}
