package com.example.util

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Mise à jour 101 — nettoyage d'affichage des titres : les compteurs de doublons
 * « (0) », « (1) », « [2] » sont masqués, les vraies informations (année, Live,
 * Remastered) restent visibles.
 */
class TrackTitleFormatterTest {

    @Test
    fun `compteur de doublon entre parenthèses masqué`() {
        assertEquals(
            "LOCO Clip officiel",
            TrackTitleFormatter.displayTitle("LOCO Clip officiel (0)")
        )
    }

    @Test
    fun `compteur entre crochets masqué`() {
        assertEquals(
            "Démo acoustique",
            TrackTitleFormatter.displayTitle("Démo acoustique [2]")
        )
    }

    @Test
    fun `année entre parenthèses conservée`() {
        assertEquals("Bella (2024)", TrackTitleFormatter.displayTitle("Bella (2024)"))
    }

    @Test
    fun `mention live conservée`() {
        assertEquals("Intro (Live)", TrackTitleFormatter.displayTitle("Intro (Live)"))
    }

    @Test
    fun `titre sans suffixe inchangé`() {
        assertEquals("Sina", TrackTitleFormatter.displayTitle("Sina"))
    }

    @Test
    fun `un seul suffixe retiré à la fois`() {
        assertEquals(
            "Chanson (Live)",
            TrackTitleFormatter.displayTitle("Chanson (Live) (1)")
        )
    }

    @Test
    fun `titre composé uniquement du compteur conservé tel quel`() {
        assertEquals("(0)", TrackTitleFormatter.displayTitle("(0)"))
    }

    @Test
    fun `titre null ou vide renvoie une chaîne vide`() {
        assertEquals("", TrackTitleFormatter.displayTitle(null))
        assertEquals("", TrackTitleFormatter.displayTitle("   "))
    }

    @Test
    fun `espaces de fin absorbés par le nettoyage`() {
        assertEquals("Wahala", TrackTitleFormatter.displayTitle("Wahala (3)  "))
    }
}
