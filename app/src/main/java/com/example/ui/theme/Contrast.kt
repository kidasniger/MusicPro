package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.pow

/** Seuil WCAG AA pour le texte normal (4,5:1). */
const val WcagMinContrastNormalText = 4.5f

/** Seuil WCAG AA pour les grands textes et les composants (3:1). */
const val WcagMinContrastLargeText = 3.0f

/** Opacité maximale du film du verre pour le filet de sécurité lisibilité (85 %). */
const val GlassMaxReadableFilmAlpha = 0.85f

/** Pas de recherche du filet de sécurité (5 % d'opacité par itération). */
const val GlassReadableFilmAlphaStep = 0.05f

/**
 * Luminance relative WCAG 2.x d'une couleur sRGB (0 à 1).
 * Voir https://www.w3.org/TR/WCAG22/#dfn-relative-luminance
 */
fun relativeLuminance(color: Color): Float {
    fun channel(c: Float): Float {
        val v = c.coerceIn(0f, 1f)
        return if (v <= 0.03928f) v / 12.92f else ((v + 0.055f) / 1.055f).pow(2.4f)
    }
    return 0.2126f * channel(color.red) + 0.7152f * channel(color.green) + 0.0722f * channel(color.blue)
}

/**
 * Rapport de contraste WCAG 2.x entre deux couleurs (1:1 à 21:1).
 * Voir https://www.w3.org/TR/WCAG22/#dfn-contrast-ratio
 */
fun wcagContrastRatio(foreground: Color, background: Color): Float {
    val l1 = relativeLuminance(foreground)
    val l2 = relativeLuminance(background)
    val lighter = maxOf(l1, l2)
    val darker = minOf(l1, l2)
    return (lighter + 0.05f) / (darker + 0.05f)
}

/** Compose cette couleur (potentiellement translucide) sur un fond opaque. */
fun Color.compositedOver(background: Color): Color {
    val a = alpha.coerceIn(0f, 1f)
    return Color(
        red = red * a + background.red * (1f - a),
        green = green * a + background.green * (1f - a),
        blue = blue * a + background.blue * (1f - a),
        alpha = 1f
    )
}

/**
 * Filet de sécurité lisibilité du verre (mise à jour 084) : derrière une surface translucide, le
 * fond réel peut être n'importe quoi (pochette claire, page blanche…). On évalue donc le pire cas
 * — contenu posé sur le film posé lui-même sur du blanc pur ou du noir pur — et on remonte
 * l'opacité du film par pas de [GlassReadableFilmAlphaStep] jusqu'à ce que le rapport atteigne
 * [minRatio], sans dépasser [maxAlpha].
 *
 * @return l'opacité du film à dessiner (jamais inférieure à [baseAlpha], jamais au-delà de [maxAlpha]).
 */
fun readableGlassFilmAlpha(
    filmColor: Color,
    baseAlpha: Float,
    contentColor: Color,
    minRatio: Float = WcagMinContrastNormalText,
    maxAlpha: Float = GlassMaxReadableFilmAlpha,
    worstCaseBackdrops: List<Color> = listOf(Color.White, Color.Black)
): Float {
    val start = baseAlpha.coerceIn(0f, 1f)
    val cap = maxAlpha.coerceIn(0f, 1f)
    if (cap <= start) return start.coerceAtMost(1f)

    fun worstRatioAt(alpha: Float): Float = worstCaseBackdrops.minOf { backdrop ->
        val effective = filmColor.copy(alpha = alpha).compositedOver(backdrop)
        wcagContrastRatio(contentColor, effective)
    }

    var alpha = start
    while (alpha < cap - 1e-4f && worstRatioAt(alpha) < minRatio) {
        alpha = (alpha + GlassReadableFilmAlphaStep).coerceAtMost(cap)
    }
    return alpha
}
