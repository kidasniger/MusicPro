package com.example.ui.adaptive

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Mise à jour 085 — largeur de fenêtre courante, calculée dans [com.example.MainActivity] avec
 * `calculateWindowSizeClass` et fournie à toute l'application.
 *
 *  - Compact  : téléphone (navigation basse en pilule de verre) ;
 *  - Medium   : tablette étroite / pliable déplié (rail latéral) ;
 *  - Expanded : tablette / ordinateur (rail latéral + contenus en deux volets).
 */
val LocalWindowWidthClass = staticCompositionLocalOf { WindowWidthSizeClass.Compact }

/** Vrai dès que la largeur mérite un rail latéral (tablette, pliable déplié, paysage large). */
fun widthSupportsSideRail(widthClass: WindowWidthSizeClass): Boolean =
    widthClass >= WindowWidthSizeClass.Medium

/** Vrai quand la largeur permet deux volets côte à côte (liste | détail). */
fun widthSupportsTwoPane(widthClass: WindowWidthSizeClass): Boolean =
    widthClass == WindowWidthSizeClass.Expanded
