package com.example.groq

import org.junit.Assert.assertEquals
import org.junit.Test

class GroqLyricsQueryAssistantTest {

    @Test
    fun parseSuggestion_normalizesKeyValueResponse() {
        val suggestion = GroqLyricsQueryAssistant.parseSuggestion(
            raw = """
                TITLE=Bohemian Rhapsody
                ARTIST=Queen
            """.trimIndent(),
            fallbackTitle = "bad title",
            fallbackArtist = "bad artist"
        )

        requireNotNull(suggestion)
        assertEquals("Bohemian Rhapsody", suggestion.title)
        assertEquals("Queen", suggestion.artist)
    }

    @Test
    fun parseSuggestion_fallsBackWhenOneFieldIsMissing() {
        val suggestion = GroqLyricsQueryAssistant.parseSuggestion(
            raw = "TITLE: Halo",
            fallbackTitle = "Halo",
            fallbackArtist = "Beyonce"
        )

        requireNotNull(suggestion)
        assertEquals("Halo", suggestion.title)
        assertEquals("Beyonce", suggestion.artist)
    }
}
