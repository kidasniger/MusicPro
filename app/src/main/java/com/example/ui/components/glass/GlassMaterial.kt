package com.example.ui.components.glass

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Description d'un « matériau » de verre : flou, voile de couleur (Film),
 * reflet haut→bas et bordure en dégradé.
 */
@Immutable
data class GlassMaterial(
    /** Rayon du flou du contenu derrière (ignoré sous API 31). */
    val blurRadius: Dp,
    /** Couleur du voile (Film). */
    val filmColor: Color,
    /** Opacité du film quand le flou est actif. */
    val filmAlpha: Float,
    /** Opacité du film quand le flou est indisponible (API < 31 ou pas de fond). */
    val fallbackFilmAlpha: Float,
    /** Couleur du contenu posé sur le verre (texte, icônes). */
    val contentColor: Color,
    val highlightTopAlpha: Float = 0.22f,
    val highlightBottomAlpha: Float = 0.04f,
    val borderStartAlpha: Float = 0.55f,
    val borderEndAlpha: Float = 0.10f,
    val borderWidth: Dp = 1.dp
)

/** Matériaux prêts à l'emploi, adaptés au thème clair ou sombre courant. */
object GlassMaterials {

    /** Barres, boutons ronds. */
    @Composable
    fun regular(): GlassMaterial = build(blur = 24.dp, dark = 0.45f, light = 0.55f)

    /** Éléments légers posés sur des visuels (pochettes). */
    @Composable
    fun thin(): GlassMaterial = build(blur = 16.dp, dark = 0.30f, light = 0.40f)

    /** Feuilles et zones de texte dense : contraste maximal. */
    @Composable
    fun thick(): GlassMaterial = build(blur = 32.dp, dark = 0.65f, light = 0.75f)

    @Composable
    private fun build(blur: Dp, dark: Float, light: Float): GlassMaterial {
        val scheme = MaterialTheme.colorScheme
        val isDark = scheme.surface.luminance() < 0.5f
        return GlassMaterial(
            blurRadius = blur,
            filmColor = scheme.surface,
            filmAlpha = if (isDark) dark else light,
            fallbackFilmAlpha = 0.92f,
            contentColor = scheme.onSurface
        )
    }
}
