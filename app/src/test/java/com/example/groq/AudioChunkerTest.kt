package com.example.groq

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AudioChunkerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testFileUnderThresholdDoesNotChunk() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val testFile = tempFolder.newFile("short_song.mp3")
        FileOutputStream(testFile).use { it.write(ByteArray(1024 * 100)) } // 100 KB

        val chunks = AudioChunker.prepareChunks(context, testFile, 180_000L)
        assertEquals(1, chunks.size)
        assertFalse(chunks[0].isTempFile)
        assertEquals(0.0, chunks[0].timeOffsetSeconds, 0.001)
        assertEquals(testFile.absolutePath, chunks[0].file.absolutePath)
    }

    @Test
    fun testEmptyOrNonExistentFileReturnsEmpty() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val nonExistent = File("/non/existent/path/song.mp3")
        val chunks = AudioChunker.prepareChunks(context, nonExistent, 0L)
        assertTrue(chunks.isEmpty())
    }

    @Test
    fun testMaxGroqFileBytesConstant() {
        // Must be <= 25MB (Groq file size limit)
        val maxBytes = AudioChunker.MAX_GROQ_FILE_BYTES
        assertTrue(maxBytes <= 25 * 1024 * 1024L)
        assertTrue(maxBytes >= 20 * 1024 * 1024L)
    }
}
