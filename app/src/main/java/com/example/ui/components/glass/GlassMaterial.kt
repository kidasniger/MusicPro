package com.example.ui.components.glass

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Recette de base d'une surface en verre : rayon de flou, voile de couleur (film), reflet et bordure.
 * Le niveau choisi par l'utilisateur ([com.example.data.preferences.GlassMaterial]) décide ensuite
 * de ce qui est réellement dessiné avec cette recette (voir [GlassSurface]).
 */
@Immutable
data class GlassSpec(
    /** Rayon du flou en QUALITY (BALANCED utilise [GlassBalancedBlurRadius], FASTEST aucun). */
    val blurRadius: Dp,
    /** Couleur du voile (film). */
    val filmColor: Color,
    /** Opacité du film quand le flou est actif. */
    val filmAlpha: Float,
    /** Opacité de l'aplat sans flou (voir [GlassFlatFallbackAlpha]). */
    val fallbackFilmAlpha: Float,
    /** Couleur du contenu posé sur le verre (texte, icônes). */
    val contentColor: Color,
    val highlightTopAlpha: Float = 0.22f,
    val highlightBottomAlpha: Float = 0.04f,
    val borderStartAlpha: Float = 0.55f,
    val borderEndAlpha: Float = 0.10f,
    val borderWidth: Dp = 1.dp
)

/** Recettes prêtes à l'emploi, adaptées au thème clair ou sombre courant. */
object GlassMaterials {

    /** Barres, boutons ronds. */
    @Composable
    fun regular(): GlassSpec = build(blur = 24.dp, dark = 0.45f, light = 0.55f)

    /** Éléments légers posés sur des visuels (pochettes). */
    @Composable
    fun thin(): GlassSpec = build(blur = 16.dp, dark = 0.30f, light = 0.40f)

    /** Feuilles et zones de texte dense : contraste maximal. */
    @Composable
    fun thick(): GlassSpec = build(blur = 32.dp, dark = 0.65f, light = 0.75f)

    @Composable
    private fun build(blur: Dp, dark: Float, light: Float): GlassSpec {
        val scheme = MaterialTheme.colorScheme
        val isDark = scheme.surface.luminance() < 0.5f
        return GlassSpec(
            blurRadius = blur,
            filmColor = scheme.surface,
            filmAlpha = if (isDark) dark else light,
            fallbackFilmAlpha = GlassFlatFallbackAlpha,
            contentColor = scheme.onSurface
        )
    }
}
