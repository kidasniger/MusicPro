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

class MediaStoreAudioScanner(private val context: Context) : AudioScanner {

    companion object {
        private const val TAG = "MediaStoreAudioScanner"
    }

    override suspend fun scanAudioFiles(): List<AudioTrackEntity> = withContext(Dispatchers.IO) {
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
        val albumIdColumn = MediaStore.Audio.AudioColumns.ALBUM_ID
        projection.add(albumIdColumn)

        // Select real audio music tracks, excluding ringtones, alarms, and notifications
        val selection = "(${MediaStore.Audio.Media.IS_MUSIC} != 0 OR ${MediaStore.Audio.Media.MIME_TYPE} LIKE 'audio/%') " +
                "AND (${MediaStore.Audio.Media.DURATION} >= 1000 OR ${MediaStore.Audio.Media.DURATION} IS NULL)"
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
                    val duration = if (cursor.isNull(durationColumn)) 0L else cursor.getLong(durationColumn)
                    val path = cursor.getString(dataColumn) ?: ""
                    val mimeType = cursor.getString(mimeTypeColumn)
                    val size = if (cursor.isNull(sizeColumn)) 0L else cursor.getLong(sizeColumn)
                    val dateAdded = if (cursor.isNull(dateAddedColumn)) 0L else cursor.getLong(dateAddedColumn)

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
                    val albumId = if (albumIdIdx != -1 && !cursor.isNull(albumIdIdx)) cursor.getLong(albumIdIdx) else -1L
                    val albumArtUri = if (albumId > 0) {
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

                    // Vérifier si le fichier physique existe réellement sur le stockage
                    // Empêche l'affichage de morceaux "fantômes" qui ont été supprimés
                    val directFile = if (path.isNotBlank()) File(path) else null
                    val existsOnDisk = directFile?.exists() ?: false

                    if (!existsOnDisk) {
                        // Si le fichier physique n'existe pas sur le disque, on vérifie si l'URI MediaStore est encore ouvrable
                        val contentAccessible = try {
                            val openUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                            context.contentResolver.openAssetFileDescriptor(openUri, "r")?.use { true } ?: false
                        } catch (e: Exception) {
                            false
                        }
                        if (!contentAccessible) {
                            // Fichier supprimé du téléphone mais encore référencé par le cache MediaStore d'Android : on l'ignore
                            continue
                        }
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
}
