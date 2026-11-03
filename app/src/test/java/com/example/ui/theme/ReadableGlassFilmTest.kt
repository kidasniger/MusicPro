package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Mise à jour 084 — filet de sécurité « Verre lisible » : l'opacité du film remonte
 * automatiquement (jusqu'à 85 %) quand le pire cas blanc/noir passe sous 4,5:1.
 */
class ReadableGlassFilmTest {

    // Paire défavorable : contenu clair sur film sombre translucide (pire cas blanc pur).
    private val darkFilm = Color(0xFF141B2D)
    private val lightContent = Color(0xFFF2F5FF)

    @Test
    fun `l opacite remonte jusqu a 4_5_1 dans le pire cas`() {
        val base = 0.45f
        val result = readableGlassFilmAlpha(darkFilm, base, lightContent)
        assertTrue("l'opacité doit augmenter : $result", result > base)
        assertTrue("plafond 85 % dépassé : $result", result <= GlassMaxReadableFilmAlpha + 1e-4f)
        // Vérifie que le résultat atteint bien le seuil sur les deux pires fonds.
        listOf(Color.White, Color.Black).forEach { backdrop ->
            val effective = darkFilm.copy(alpha = result).compositedOver(backdrop)
            val ratio = wcagContrastRatio(lightContent, effective)
            assertTrue(
                "contraste %.2f:1 sous 4,5:1 après remontée".format(ratio),
                ratio >= WcagMinContrastNormalText
            )
        }
    }

    @Test
    fun `une base déjà lisible reste inchangée`() {
        // Film quasi opaque : le contraste passe déjà, aucune remontée nécessaire.
        val result = readableGlassFilmAlpha(darkFilm, 0.86f, lightContent)
        assertEquals(0.86f, result, 1e-4f)
    }

    @Test
    fun `le plafond de 85 pour cent est respecté même si le seuil est inatteignable`() {
        // Contenu gris moyen sur film gris moyen : aucun alpha ne suffit.
        val midGray = Color(0xFF808080)
        val result = readableGlassFilmAlpha(midGray, 0.45f, midGray)
        assertEquals(GlassMaxReadableFilmAlpha, result, 1e-4f)
    }

    @Test
    fun `pas de remontée en dessous du plafond quand la base est supérieure`() {
        val result = readableGlassFilmAlpha(darkFilm, 0.95f, lightContent)
        assertEquals(0.95f, result, 1e-4f)
    }
}
