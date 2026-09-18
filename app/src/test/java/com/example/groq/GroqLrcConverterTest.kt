package com.example.groq

import com.example.lyrics.LrcParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GroqLrcConverterTest {

    @Test
    fun testFormatTimestamp() {
        assertEquals("[00:00.00]", GroqLrcConverter.formatTimestamp(0.0))
        assertEquals("[00:01.50]", GroqLrcConverter.formatTimestamp(1.5))
        assertEquals("[01:15.25]", GroqLrcConverter.formatTimestamp(75.25))
        assertEquals("[03:45.89]", GroqLrcConverter.formatTimestamp(225.89))
    }

    @Test
    fun testConvertSegmentsToLrc() {
        val segments = listOf(
            GroqSegment(
                id = 0,
                start = 0.0,
                end = 3.5,
                text = " Hello world"
            ),
            GroqSegment(
                id = 1,
                start = 4.2,
                end = 7.8,
                text = " Welcome to MusicPro"
            ),
            GroqSegment(
                id = 2,
                start = 8.0,
                end = 12.0,
                text = " Enjoy the neon beat"
            )
        )

        val lrcString = GroqLrcConverter.convertToLrc(
            segments = segments,
            title = "Cyber Neon",
            artist = "Retro Synth",
            album = "Futuresynth 2026"
        )

        assertTrue(lrcString.contains("[ti:Cyber Neon]"))
        assertTrue(lrcString.contains("[ar:Retro Synth]"))
        assertTrue(lrcString.contains("[al:Futuresynth 2026]"))
        assertTrue(lrcString.contains("[by:MusicPro Whisper large-v3]"))
        assertTrue(lrcString.contains("[00:00.00] Hello world"))
        assertTrue(lrcString.contains("[00:04.20] Welcome to MusicPro"))
        assertTrue(lrcString.contains("[00:08.00] Enjoy the neon beat"))

        // Round-trip parse test with LrcParser
        val parsed = LrcParser.parse(lrcString)
        assertEquals("Cyber Neon", parsed.title)
        assertEquals("Retro Synth", parsed.artist)
        assertEquals(3, parsed.lines.size)
        assertEquals(0L, parsed.lines[0].timeMs)
        assertEquals("Hello world", parsed.lines[0].text)
        assertEquals(4_200L, parsed.lines[1].timeMs)
        assertEquals("Welcome to MusicPro", parsed.lines[1].text)
    }

    @Test
    fun testSegmentTimestampLrcProperty() {
        val segment = GroqSegment(
            id = 5,
            start = 65.5,
            end = 68.2,
            text = " Neon city in the rain "
        )

        assertEquals("[01:05.50]", segment.timestampLrc)
        assertEquals("Neon city in the rain", segment.cleanText)
    }
}
