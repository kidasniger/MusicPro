package com.example.data.scanner

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FileNameMetadataTest {

    @Test
    fun underscoresBecomeSpacesAndTheFirstPartIsTheArtist() {
        assertEquals(
            FileNameMetadata("La Rvfleuze", "Serrure #1(0)"),
            metadataFromFileName("La_Rvfleuze_-_Serrure_#1(0)")
        )
    }

    @Test
    fun onlyTheFirstSeparatorSplitsArtistFromTitle() {
        assertEquals(
            FileNameMetadata("Daft Punk", "Around the World - Radio Edit"),
            metadataFromFileName("Daft Punk - Around the World - Radio Edit")
        )
    }

    @Test
    fun aLeadingTrackNumberIsIgnored() {
        assertEquals(
            FileNameMetadata("Artiste", "Titre"),
            metadataFromFileName("01 - Artiste - Titre")
        )
        assertNull(metadataFromFileName("01 - Titre seul"))
    }

    @Test
    fun namesWithoutTheModelGiveNothing() {
        assertNull(metadataFromFileName("AUD-20260805-WA0012"))
        assertNull(metadataFromFileName("Juste un titre"))
        assertNull(metadataFromFileName(" - Titre"))
        assertNull(metadataFromFileName("Artiste - "))
    }

    @Test
    fun aVeryLongFirstPartIsNotAnArtist() {
        val sentence = "a".repeat(MaxFileNameArtistLength + 1)
        assertNull(metadataFromFileName("$sentence - Titre"))
    }
}
