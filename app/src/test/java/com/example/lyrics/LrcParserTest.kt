package com.example.lyrics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LrcParserTest {

    @Test
    fun testParseStandardLrc() {
        val sampleLrc = """
            [ti:Cyber City Lights]
            [ar:Neon Wave]
            [al:Synthwave Odyssey]
            [offset:50]
            [00:00.00]Intro
            [00:12.50]Première ligne
            [00:24.00]Deuxième ligne
            [01:05.123]Troisième ligne
        """.trimIndent()

        val lyrics = LrcParser.parse(sampleLrc)

        assertEquals("Cyber City Lights", lyrics.title)
        assertEquals("Neon Wave", lyrics.artist)
        assertEquals("Synthwave Odyssey", lyrics.album)
        assertEquals(50L, lyrics.offsetMs)
        assertEquals(4, lyrics.lines.size)

        assertEquals(0L, lyrics.lines[0].timeMs)
        assertEquals("Intro", lyrics.lines[0].text)

        assertEquals(12_500L, lyrics.lines[1].timeMs)
        assertEquals("Première ligne", lyrics.lines[1].text)

        assertEquals(24_000L, lyrics.lines[2].timeMs)
        assertEquals(65_123L, lyrics.lines[3].timeMs)
    }

    @Test
    fun testMultiTimestampOnSameLine() {
        val sample = """
            [00:10.00][00:20.00]Refrain répété
        """.trimIndent()

        val lyrics = LrcParser.parse(sample)
        assertEquals(2, lyrics.lines.size)
        assertEquals(10_000L, lyrics.lines[0].timeMs)
        assertEquals("Refrain répété", lyrics.lines[0].text)
        assertEquals(20_000L, lyrics.lines[1].timeMs)
        assertEquals("Refrain répété", lyrics.lines[1].text)
    }

    @Test
    fun testFindActiveLineIndex() {
        val lines = listOf(
            LyricLine(0L, "Line 0"),
            LyricLine(10_000L, "Line 1"),
            LyricLine(25_000L, "Line 2"),
            LyricLine(40_000L, "Line 3")
        )
        val data = LyricsData(lines = lines)

        assertEquals(0, data.findActiveLineIndex(0L))
        assertEquals(0, data.findActiveLineIndex(5_000L))
        assertEquals(1, data.findActiveLineIndex(10_000L))
        assertEquals(1, data.findActiveLineIndex(20_000L))
        assertEquals(2, data.findActiveLineIndex(25_000L))
        assertEquals(2, data.findActiveLineIndex(39_999L))
        assertEquals(3, data.findActiveLineIndex(40_000L))
        assertEquals(3, data.findActiveLineIndex(100_000L))
    }
}
