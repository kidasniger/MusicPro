package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.data.preferences.AppStyle
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Mise à jour 083 — contraste WCAG vérifié automatiquement : chaque paire texte/fond de chaque
 * thème (Minimal, Néon, Verre × clair/sombre) doit atteindre 4,5:1 (3:1 pour les grands textes).
 * Sur le verre, le pire cas est évalué sur fond blanc et noir pur (la pochette derrière le verre
 * peut être n'importe quoi).
 */
class ContrastWcagTest {

    private val schemes: List<Pair<String, androidx.compose.material3.ColorScheme>> = listOf(
        "Minimal clair" to musicProBaseScheme(AppStyle.MINIMAL, isDark = false),
        "Minimal sombre" to musicProBaseScheme(AppStyle.MINIMAL, isDark = true),
        "Néon clair" to musicProBaseScheme(AppStyle.NEON, isDark = false),
        "Néon sombre" to musicProBaseScheme(AppStyle.NEON, isDark = true),
        "Verre clair" to musicProBaseScheme(AppStyle.GLASS, isDark = false),
        "Verre sombre" to musicProBaseScheme(AppStyle.GLASS, isDark = true)
    )

    /** Paires texte/fond exigées à 4,5:1 (texte normal). */
    private fun textPairs(s: androidx.compose.material3.ColorScheme): List<Pair<String, Pair<Color, Color>>> =
        listOf(
            "onBackground/background" to (s.onBackground to s.background),
            "onSurface/surface" to (s.onSurface to s.surface),
            "onSurfaceVariant/surface" to (s.onSurfaceVariant to s.surface),
            "onSurfaceVariant/surfaceVariant" to (s.onSurfaceVariant to s.surfaceVariant),
            "onPrimary/primary" to (s.onPrimary to s.primary),
            "onPrimaryContainer/primaryContainer" to (s.onPrimaryContainer to s.primaryContainer),
            "onSecondary/secondary" to (s.onSecondary to s.secondary),
            "onSecondaryContainer/secondaryContainer" to (s.onSecondaryContainer to s.secondaryContainer),
            "onTertiary/tertiary" to (s.onTertiary to s.tertiary),
            "onTertiaryContainer/tertiaryContainer" to (s.onTertiaryContainer to s.tertiaryContainer),
            "onError/error" to (s.onError to s.error),
            "onErrorContainer/errorContainer" to (s.onErrorContainer to s.errorContainer),
            "inverseOnSurface/inverseSurface" to (s.inverseOnSurface to s.inverseSurface)
        )

    @Test
    fun `chaque paire texte-fond de chaque theme atteint 4_5_1`() {
        schemes.forEach { (name, scheme) ->
            textPairs(scheme).forEach { (pairName, colors) ->
                val (foreground, background) = colors
                // Les couleurs translucides (ex. conteneurs néon à 15 %) sont dessinées sur la
                // surface : on les compose d'abord sur le fond réel, puis le texte dessus.
                val effectiveBg =
                    if (background.alpha < 1f) background.compositedOver(scheme.surface)
                    else background
                val fg = foreground.compositedOver(effectiveBg)
                val ratio = wcagContrastRatio(fg, effectiveBg)
                assertTrue(
                    "$name — $pairName : contraste %.2f:1 < 4,5:1 (fg=%s bg=%s)".format(
                        ratio,
                        fg,
                        effectiveBg
                    ),
                    ratio >= WcagMinContrastNormalText
                )
            }
        }
    }

    @Test
    fun `les couleurs de composant atteignent 3_1 sur leur fond`() {
        schemes.forEach { (name, scheme) ->
            val pairs = listOf(
                "primary/surface" to (scheme.primary to scheme.surface),
                "error/surface" to (scheme.error to scheme.surface)
            )
            pairs.forEach { (pairName, colors) ->
                val (fg, bg) = colors
                val ratio = wcagContrastRatio(fg.compositedOver(bg), bg)
                assertTrue(
                    "$name — $pairName : contraste %.2f:1 < 3:1".format(ratio),
                    ratio >= WcagMinContrastLargeText
                )
            }
        }
    }

    /**
     * Verre : le contenu posé sur le film, lui-même posé sur le pire fond (blanc ou noir pur),
     * reste lisible grâce au filet de sécurité (mise à jour 084).
     */
    @Test
    fun `le verre reste lisible dans le pire cas blanc et noir`() {
        val glassSchemes = listOf(
            "Verre clair" to musicProBaseScheme(AppStyle.GLASS, isDark = false),
            "Verre sombre" to musicProBaseScheme(AppStyle.GLASS, isDark = true)
        )
        glassSchemes.forEach { (name, scheme) ->
            val filmColor = scheme.surface
            val contentColor = scheme.onSurface
            val worstCases = listOf(Color.White, Color.Black)
            listOf(0.30f, 0.45f, 0.55f, 0.65f).forEach { baseAlpha ->
                val alpha = readableGlassFilmAlpha(filmColor, baseAlpha, contentColor)
                assertTrue(
                    "$name : opacité > 85 %",
                    alpha <= GlassMaxReadableFilmAlpha + 1e-4f
                )
                worstCases.forEach { backdrop ->
                    val effective = filmColor.copy(alpha = alpha).compositedOver(backdrop)
                    val ratio = wcagContrastRatio(contentColor, effective)
                    assertTrue(
                        "$name — film %.2f sur %s : contraste %.2f:1 < 4,5:1".format(
                            alpha, if (backdrop == Color.White) "blanc" else "noir", ratio
                        ),
                        ratio >= WcagMinContrastNormalText
                    )
                }
            }
        }
    }
}
