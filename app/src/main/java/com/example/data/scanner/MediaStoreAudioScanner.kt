package com.example.data.scanner

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.example.data.local.AudioTrackEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MediaStoreAudioScanner(private val context: Context) {

    companion object {
        private const val TAG = "MediaStoreAudioScanner"
    }

    suspend fun scanAudioFiles(): List<AudioTrackEntity> = withContext(Dispatchers.IO) {
        val tracksList = mutableListOf<AudioTrackEntity>()

        val collectionUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = mutableListOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED
        )

        // Add ALBUM_ID for album artwork URI
        val albumIdColumn = "album_id"
        projection.add(albumIdColumn)

        // Select only real music tracks with duration > 5 seconds
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 5000"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            context.contentResolver.query(
                collectionUri,
                projection.toTypedArray(),
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val albumIdIdx = cursor.getColumnIndex(albumIdColumn)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val rawTitle = cursor.getString(titleColumn) ?: "Titre Inconnu"
                    val rawArtist = cursor.getString(artistColumn) ?: "Artiste Inconnu"
                    val rawAlbum = cursor.getString(albumColumn) ?: "Album Inconnu"
                    val duration = cursor.getLong(durationColumn)
                    val path = cursor.getString(dataColumn) ?: ""
                    val mimeType = cursor.getString(mimeTypeColumn)
                    val size = cursor.getLong(sizeColumn)
                    val dateAdded = cursor.getLong(dateAddedColumn)

                    // Normalize unknown values from Android MediaStore
                    val title = if (rawTitle.isBlank() || rawTitle == "<unknown>") {
                        File(path).nameWithoutExtension.ifBlank { "Titre Inconnu" }
                    } else rawTitle

                    val artist = if (rawArtist.isBlank() || rawArtist == "<unknown>") {
                        "Artiste Inconnu"
                    } else rawArtist

                    val album = if (rawAlbum.isBlank() || rawAlbum == "<unknown>") {
                        "Album Inconnu"
                    } else rawAlbum

                    // Construct Album Art Uri
                    val albumId = if (albumIdIdx != -1) cursor.getLong(albumIdIdx) else -1L
                    val albumArtUri = if (albumId != -1L) {
                        ContentUris.withAppendedId(
                            Uri.parse("content://media/external/audio/albumart"),
                            albumId
                        ).toString()
                    } else null

                    // Content URI for playback
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    ).toString()

                    // Extract Folder
                    val folder = try {
                        val file = File(path)
                        file.parentFile?.name ?: "Musique"
                    } catch (e: Exception) {
                        "Musique"
                    }

                    // Check for existing synced .lrc file next to audio file
                    val hasSyncedLyrics = try {
                        if (path.isNotBlank()) {
                            val dotIndex = path.lastIndexOf('.')
                            if (dotIndex != -1) {
                                val lrcPath = path.substring(0, dotIndex) + ".lrc"
                                File(lrcPath).exists()
                            } else false
                        } else false
                    } catch (e: Exception) {
                        false
                    }

                    tracksList.add(
                        AudioTrackEntity(
                            id = id,
                            title = title,
                            artist = artist,
                            album = album,
                            duration = duration,
                            contentUri = contentUri,
                            albumArtUri = albumArtUri,
                            path = path,
                            folder = folder,
                            mimeType = mimeType,
                            size = size,
                            hasSyncedLyrics = hasSyncedLyrics,
                            dateAdded = dateAdded
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors du scan MediaStore: ${e.message}", e)
        }

        tracksList
    }

    /**
     * Fournit un catalogue d'échantillons haute fidélité pour émulateur / premier test
     * lorsque l'appareil ne contient pas encore de fichiers audio locaux.
     */
    fun getFallbackDemoTracks(): List<AudioTrackEntity> {
        return listOf(
            AudioTrackEntity(
                id = 1001L,
                title = "Neon Horizons (Synthwave Mix)",
                artist = "Cyber Pulse",
                album = "Neon Drift 2088",
                duration = 225000L,
                contentUri = "content://media/external/audio/media/1001",
                albumArtUri = null,
                path = "/storage/emulated/0/Music/Pop/Neon_Horizons.flac",
                folder = "Pop",
                mimeType = "audio/flac",
                size = 34500000L,
                hasSyncedLyrics = true,
                dateAdded = System.currentTimeMillis()
            ),
            AudioTrackEntity(
                id = 1002L,
                title = "Electric Aurora",
                artist = "Luna & The Starlight",
                album = "Starlight Odyssey",
                duration = 252000L,
                contentUri = "content://media/external/audio/media/1002",
                albumArtUri = null,
                path = "/storage/emulated/0/Music/Pop/Electric_Aurora.mp3",
                folder = "Pop",
                mimeType = "audio/mpeg",
                size = 10240000L,
                hasSyncedLyrics = true,
                dateAdded = System.currentTimeMillis()
            ),
            AudioTrackEntity(
                id = 1003L,
                title = "Midnight City Ride",
                artist = "Kavinsky Wave",
                album = "Retro Overdrive",
                duration = 198000L,
                contentUri = "content://media/external/audio/media/1003",
                albumArtUri = null,
                path = "/storage/emulated/0/Music/Dance/Midnight_City_Ride.wav",
                folder = "Dance",
                mimeType = "audio/wav",
                size = 45200000L,
                hasSyncedLyrics = true,
                dateAdded = System.currentTimeMillis()
            ),
            AudioTrackEntity(
                id = 1004L,
                title = "Retro Wave Dreams",
                artist = "Synth Master",
                album = "Neon Drift 2088",
                duration = 304000L,
                contentUri = "content://media/external/audio/media/1004",
                albumArtUri = null,
                path = "/storage/emulated/0/Music/Electro/Retro_Wave_Dreams.flac",
                folder = "Electro",
                mimeType = "audio/flac",
                size = 48100000L,
                hasSyncedLyrics = false,
                dateAdded = System.currentTimeMillis()
            ),
            AudioTrackEntity(
                id = 1005L,
                title = "Hyperdrive Odyssey",
                artist = "Orbit Velocity",
                album = "Cosmic Journey",
                duration = 176000L,
                contentUri = "content://media/external/audio/media/1005",
                albumArtUri = null,
                path = "/storage/emulated/0/Download/Hyperdrive_Odyssey.mp3",
                folder = "Download",
                mimeType = "audio/mpeg",
                size = 8400000L,
                hasSyncedLyrics = true,
                dateAdded = System.currentTimeMillis()
            ),
            AudioTrackEntity(
                id = 1006L,
                title = "Dark Cybernetic Void",
                artist = "Void Walker",
                album = "Void Protocol",
                duration = 275000L,
                contentUri = "content://media/external/audio/media/1006",
                albumArtUri = null,
                path = "/storage/emulated/0/Music/Rock/Cybernetic_Void.flac",
                folder = "Rock",
                mimeType = "audio/flac",
                size = 52000000L,
                hasSyncedLyrics = true,
                dateAdded = System.currentTimeMillis()
            ),
            AudioTrackEntity(
                id = 1007L,
                title = "Blinding Starlight",
                artist = "The Weeknd Style",
                album = "After Hours Tribute",
                duration = 200000L,
                contentUri = "content://media/external/audio/media/1007",
                albumArtUri = null,
                path = "/storage/emulated/0/Music/Pop/Blinding_Starlight.mp3",
                folder = "Pop",
                mimeType = "audio/mpeg",
                size = 9800000L,
                hasSyncedLyrics = true,
                dateAdded = System.currentTimeMillis()
            ),
            AudioTrackEntity(
                id = 1008L,
                title = "Summer Breeze Escape",
                artist = "Luna & The Starlight",
                album = "Starlight Odyssey",
                duration = 210000L,
                contentUri = "content://media/external/audio/media/1008",
                albumArtUri = null,
                path = "/storage/emulated/0/Music/Pop/Summer_Breeze.m4a",
                folder = "Pop",
                mimeType = "audio/m4a",
                size = 11200000L,
                hasSyncedLyrics = false,
                dateAdded = System.currentTimeMillis()
            )
        )
    }
}
