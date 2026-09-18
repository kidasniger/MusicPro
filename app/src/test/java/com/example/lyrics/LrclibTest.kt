package com.example.lyrics

import com.example.lyrics.remote.LrclibSearchResult
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class LrclibTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun testLrclibJsonParsing() {
        val json = """
            [
              {
                "id": 12345,
                "name": "Blinding Lights",
                "trackName": "Blinding Lights",
                "artistName": "The Weeknd",
                "albumName": "After Hours",
                "duration": 200.0,
                "instrumental": false,
                "plainLyrics": "I've been trying to call...",
                "syncedLyrics": "[00:00.00]Intro\n[00:15.50]I've been trying to call"
              }
            ]
        """.trimIndent()

        val listType = Types.newParameterizedType(List::class.java, LrclibSearchResult::class.java)
        val adapter = moshi.adapter<List<LrclibSearchResult>>(listType)
        val results = adapter.fromJson(json)

        assertNotNull(results)
        assertEquals(1, results!!.size)

        val result = results[0]
        assertEquals("Blinding Lights", result.displayTitle)
        assertEquals("The Weeknd", result.displayArtist)
        assertEquals("After Hours", result.displayAlbum)
        assertEquals("03:20", result.durationFormatted)
        assertTrue(result.hasSyncedLyrics)
        assertTrue(result.hasPlainLyrics)
    }

    @Test
    fun testId3SyltWriterFallbackToLrc() {
        val rootDir = tempFolder.newFolder("music")
        val fakeAudioFile = File(rootDir, "test_track.flac")
        fakeAudioFile.writeText("fake audio content")

        val searchResult = LrclibSearchResult(
            id = 999,
            trackName = "Test Song",
            artistName = "Artist",
            duration = 180.0,
            syncedLyrics = "[00:01.00]Line one\n[00:05.00]Line two"
        )

        val saveResult = Id3SyltWriter.saveLyrics(
            audioPath = fakeAudioFile.absolutePath,
            result = searchResult,
            fallbackDirectory = rootDir
        )

        // As the file is a .flac (not .mp3), it should save a companion .lrc file
        assertTrue(saveResult is LyricsSaveResult.LrcFileSuccess)
        val lrcResult = saveResult as LyricsSaveResult.LrcFileSuccess

        val expectedLrc = File(rootDir, "test_track.lrc")
        assertTrue("Le fichier test_track.lrc doit exister", expectedLrc.exists())
        assertEquals(expectedLrc.absolutePath, lrcResult.lrcPath)
        assertEquals(2, lrcResult.linesCount)

        val content = expectedLrc.readText()
        assertTrue(content.contains("[00:01.00]Line one"))
        assertTrue(content.contains("[00:05.00]Line two"))
    }
}
