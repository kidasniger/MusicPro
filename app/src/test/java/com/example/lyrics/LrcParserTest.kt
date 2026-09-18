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

    @Test
    fun testParseEmptyAndBlankContent() {
        val emptyLyrics = LrcParser.parse("")
        assertTrue(emptyLyrics.lines.isEmpty())
        assertEquals(null, emptyLyrics.title)
        assertEquals(null, emptyLyrics.artist)
        assertEquals(0L, emptyLyrics.offsetMs)

        val blankLyrics = LrcParser.parse("   \n\n  \t  \n")
        assertTrue(blankLyrics.lines.isEmpty())
    }

    @Test
    fun testParseMalformedLinesAndComments() {
        val malformed = """
            # Ceci est un commentaire
            // Un autre commentaire non-standard
            [by:LrcMaker v2]
            [re:MusicPro]
            Pas de timestamp ici du tout
            [invalide:format]
            [00:04.5]Ligne avec 1 seul chiffre décimal
            [00:08]Ligne sans décimales
            [99:59.99]Dernière ligne très lointaine
        """.trimIndent()

        val lyrics = LrcParser.parse(malformed)
        // Seules les 3 lignes horodatées valides doivent être extraites
        assertEquals(3, lyrics.lines.size)

        // [00:04.5] -> 4s + 500ms = 4500ms
        assertEquals(4_500L, lyrics.lines[0].timeMs)
        assertEquals("Ligne avec 1 seul chiffre décimal", lyrics.lines[0].text)

        // [00:08] -> 8000ms
        assertEquals(8_000L, lyrics.lines[1].timeMs)
        assertEquals("Ligne sans décimales", lyrics.lines[1].text)

        // [99:59.99] -> 99*60*1000 + 59*1000 + 990 = 5940000 + 59000 + 990 = 5999990ms
        assertEquals(5_999_990L, lyrics.lines[2].timeMs)
    }

    @Test
    fun testParseNegativeOffsetAndSorting() {
        // Test timestamps hors ordre : le parseur doit les trier chronologiquement
        val disordered = """
            [offset:-500]
            [00:30.00]Deuxième moment
            [00:10.00]Premier moment
            [00:50.00]Troisième moment
        """.trimIndent()

        val lyrics = LrcParser.parse(disordered)
        assertEquals(-500L, lyrics.offsetMs)
        assertEquals(3, lyrics.lines.size)

        // Doit être trié par ordre croissant de timeMs
        assertEquals(10_000L, lyrics.lines[0].timeMs)
        assertEquals("Premier moment", lyrics.lines[0].text)
        assertEquals(30_000L, lyrics.lines[1].timeMs)
        assertEquals("Deuxième moment", lyrics.lines[1].text)
        assertEquals(50_000L, lyrics.lines[2].timeMs)
        assertEquals("Troisième moment", lyrics.lines[2].text)
    }

    @Test
    fun testParseColonFractionSeparators() {
        // Certains fichiers LRC utilisent le format [mm:ss:xx] au lieu de [mm:ss.xx]
        val colonFraction = """
            [01:15:50]Format avec deux-points
        """.trimIndent()

        val lyrics = LrcParser.parse(colonFraction)
        assertEquals(1, lyrics.lines.size)
        // 1 min (60000) + 15 sec (15000) + 50 (500ms) = 75500ms
        assertEquals(75_500L, lyrics.lines[0].timeMs)
        assertEquals("Format avec deux-points", lyrics.lines[0].text)
    }
}
