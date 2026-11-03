package com.example.ui.audio

import com.example.data.local.AudioTrackEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Mise à jour 054/053 — utilitaires purs de la recherche : historique limité
 * à 20 entrées avec dédoublonnage insensible à la casse, décennies tapées
 * comme requêtes, meilleur résultat et lettres de l'index A–Z (056).
 */
class SearchUtilsTest {

    private fun track(id: Long, title: String, artist: String = "Artiste") = AudioTrackEntity(
        id = id,
        title = title,
        artist = artist,
        album = "Album",
        duration = 180_000L,
        contentUri = "content://media/audio/$id"
    )

    // ---- Historique (054) ----

    @Test
    fun `la nouvelle requête passe en tête d'historique`() {
        val merged = SearchUtils.mergeSearchHistory(listOf("alpha", "bravo"), "charlie")
        assertEquals(listOf("charlie", "alpha", "bravo"), merged)
    }

    @Test
    fun `les doublons insensibles à la casse sont dédupliqués`() {
        val merged = SearchUtils.mergeSearchHistory(listOf("Alpha", "alpha", "BRAVO"), "alpha")
        assertEquals(listOf("alpha", "BRAVO"), merged)
    }

    @Test
    fun `l'historique est limité à 20 entrées`() {
        var history = (30 downTo 1).map { "requête $it" }
        history = SearchUtils.mergeSearchHistory(history, "nouvelle", limit = 20)
        assertEquals(20, history.size)
        assertEquals("nouvelle", history.first())
        // 19 anciennes entrées conservées : « requête 30 » … « requête 12 ».
        assertEquals("requête 12", history.last())
    }

    @Test
    fun `une requête vide est ignorée`() {
        val existing = listOf("alpha", "bravo")
        assertEquals(existing, SearchUtils.mergeSearchHistory(existing, "   "))
    }

    @Test
    fun `une entrée est retirée sans tenir compte de la casse`() {
        val history = listOf("Alpha", "bravo", "Charlie")
        assertEquals(listOf("bravo", "Charlie"), SearchUtils.removeEntry(history, "ALPHA"))
    }

    // ---- Décennies (054) ----

    @Test
    fun `la puce Années 2020 couvre la décennie complète`() {
        assertEquals(2020..2029, SearchUtils.decadeRangeForQuery("Années 2020"))
        assertEquals(1990..1999, SearchUtils.decadeRangeForQuery("années 1990"))
        assertEquals(2000..2009, SearchUtils.decadeRangeForQuery("ANNÉES 2000"))
    }

    @Test
    fun `une requête ordinaire n'est pas une décennie`() {
        assertNull(SearchUtils.decadeRangeForQuery("Neon Nights"))
        assertNull(SearchUtils.decadeRangeForQuery(""))
    }

    // ---- Meilleur résultat (053) ----

    @Test
    fun `le titre commençant par la requête gagne`() {
        val tracks = listOf(track(1, "Midnight Drive"), track(2, "My Midnight"), track(3, "Neon"))
        assertEquals(1, SearchUtils.bestMatchIndex(tracks, "my"))
    }

    @Test
    fun `à défaut un titre contenant la requête est retenu`() {
        val tracks = listOf(track(1, "Drive Slow"), track(2, "Midnight Drive"))
        assertEquals(1, SearchUtils.bestMatchIndex(tracks, "midnight"))
    }

    @Test
    fun `liste vide ou requête vide n'ont pas de meilleur résultat`() {
        assertEquals(-1, SearchUtils.bestMatchIndex(emptyList(), "x"))
        assertTrue(SearchUtils.bestMatchIndex(listOf(track(1, "A")), "  ") == -1)
    }

    // ---- Index A–Z (056) ----

    @Test
    fun `les lettres sont triées et uniques avec étoile finale`() {
        val letters = SearchUtils.lettersFor(listOf("Neon", "alpha", "Zulu", "42 Street", "Midi"))
        assertEquals(listOf('A', 'M', 'N', 'Z', '#'), letters)
    }

    @Test
    fun `un titre vide tombe sous l'étoile`() {
        assertEquals('#', SearchUtils.firstLetterOf("   "))
        assertEquals('N', SearchUtils.firstLetterOf("neon"))
    }
}
