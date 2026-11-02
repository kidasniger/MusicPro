package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Jetons de design centralisés (mise à jour 015). Les écrans migrés n'ont pas de valeur en dur :
 * ils utilisent ces jetons. `DesignTokensGridTest` fait échouer le build si un fichier migré
 * contient une valeur dp hors grille.
 */
object MusicProTokens {

    /** Espacements : 4, 8, 12, 16, 24, 32 dp. */
    object Spacing {
        val Xs = 4.dp
        val Sm = 8.dp
        val Md = 12.dp
        val Lg = 16.dp
        val Xl = 24.dp
        val Xxl = 32.dp
        val all = listOf(Xs, Sm, Md, Lg, Xl, Xxl)
    }

    /** Rayons : 8, 12, 16, 24 dp et pilule. */
    object Radius {
        val Sm = 8.dp
        val Md = 12.dp
        val Lg = 16.dp
        val Xl = 24.dp
        val all = listOf(Sm, Md, Lg, Xl)

        /** Pilule : rayon égal à la moitié de la hauteur. */
        val PillShape = RoundedCornerShape(percent = 50)
    }

    /** Élévations : 0, 1, 3 dp (plus la lueur propre au style Néon). */
    object Elevation {
        val None = 0.dp
        val Low = 1.dp
        val Raised = 3.dp
        val all = listOf(None, Low, Raised)

        /** Lueur des cartes du style Néon uniquement (look historique de l'application). */
        val NeonGlow = 8.dp
    }

    /** Durées d'animation en ms : 120, 200, 320, plus les fondus de thème et d'accent. */
    object Duration {
        const val Fast = 120
        const val Normal = 200
        const val Slow = 320
        val all = listOf(Fast, Normal, Slow)

        /** Fondu lors d'un changement de style ou d'apparence (mise à jour 014). */
        const val ThemeFade = 250

        /** Fondu de l'accent « Selon la pochette » (mise à jour 017). */
        const val AccentFade = 400
    }

    /** Tailles de composants (zones tactiles, icônes, cartes d'aperçu). */
    object Size {
        val Stroke = 1.dp
        val StrokeSelected = 2.dp
        val Touch = 48.dp
        val IconSm = 16.dp
        val Icon = 20.dp
        val AccentDot = 16.dp
        val StylePreviewWidth = 96.dp
        val StylePreviewHeight = 140.dp

        /** Pochette des carrousels (mise à jour 021) : 140 dp. */
        val CoverCarousel = 140.dp

        /** Icône de remplacement quand une pochette est absente. */
        val CoverIcon = 40.dp

        /** Pastille d'illustration des états vides. */
        val Illustration = 96.dp

        /** Icône au centre de la pastille d'illustration. */
        val IllustrationIcon = 48.dp

        /** Photo plein largeur de l'écran Artiste (mise à jour 024). */
        val HeroHeight = 384.dp
    }
}
