package com.example.ui.components.glass

import android.os.Build
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.preferences.DefaultGlassActiveTabOpacity
import com.example.data.preferences.DefaultGlassColorStrength
import com.example.data.preferences.DefaultGlassFilmOpacity
import com.example.data.preferences.DefaultGlassFrostDp
import com.example.data.preferences.GlassMaterial
import com.example.data.preferences.GlassTintSaturation
import com.example.data.preferences.GlassTintValue
import com.example.data.preferences.clampGlassTintHue

/** Vrai si l'appareil sait flouter en temps réel (RenderEffect, Android 12 / API 31 et plus). */
val glassBlurSupported: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

/** Niveau d'effet verre choisi dans les réglages ; fourni une fois pour toute l'application. */
val LocalGlassMaterial = staticCompositionLocalOf { GlassMaterial.BALANCED }

/** Réduction des animations activée dans les réglages de MusicPro. */
val LocalReduceAnimations = staticCompositionLocalOf { false }

/**
 * Force de couleur du verre (0 à 100 %), réglée par le curseur « Couleur du verre ».
 * Elle pilote la saturation de ce qui transparaît derrière le verre (voir `glassSaturation`).
 */
val LocalGlassColorStrength = staticCompositionLocalOf { DefaultGlassColorStrength }

/**
 * Givre du verre (0 à 24 dp), réglé par le curseur « Givre » : rayon du flou des niveaux
 * BALANCED et QUALITY. Sous 2 dp, le flou est désactivé (voir `glassBlurActive`).
 */
val LocalGlassFrostDp = staticCompositionLocalOf { DefaultGlassFrostDp }

/**
 * Teinte du verre (0 à 360°), réglée par les pastilles et la barre arc-en-ciel « Teinte du verre » ;
 * `null` = Aucune. Le voile teinté est posé sur le film avec une opacité de film x 0,5
 * (voir `glassTintAlpha`).
 */
val LocalGlassTintHue = staticCompositionLocalOf<Float?> { null }

/**
 * Réglages de style du verre lus par [GlassSurface] et [GlassNavBar] (mise à jour 009), réunis dans
 * un seul objet pour ne pas passer de paramètres d'un composant à l'autre.
 *
 *  - [filmOpacity] : curseur « Film », 0 à 100 %, 50 % par défaut (film de la recette inchangé).
 *  - [activeTabOpacity] : curseur « Onglet actif », 0 à 100 %, 26 % par défaut (capsule de l'onglet
 *    sélectionné).
 *  - [readableText] : mise à jour 084 — interrupteur « Texte plus lisible » : force un film quasi
 *    opaque ([GlassMaxReadableFilmAlpha]) pour un contraste maximal sur le verre.
 *  - [followReduceTransparency] : mise à jour 084 — interrupteur « Réduire la transparence » :
 *    quand il est actif, l'application suit le réglage d'accessibilité du système (verre plus
 *    opaque, sans flou temps réel).
 */
@Immutable
data class GlassStyle(
    val filmOpacity: Int = DefaultGlassFilmOpacity,
    val activeTabOpacity: Int = DefaultGlassActiveTabOpacity,
    val readableText: Boolean = false,
    val followReduceTransparency: Boolean = true
)

/** Style du verre fourni une fois pour toute l'application ; valeurs par défaut sans fournisseur. */
val LocalGlassStyle = compositionLocalOf { GlassStyle() }

/** Couleur du voile pour une teinte donnée (saturation et luminosité fixes, voir GlassTint.kt). */
fun glassTintColor(hue: Float): Color =
    Color.hsv(clampGlassTintHue(hue), GlassTintSaturation, GlassTintValue)

/** Opacité de l'aplat translucide de FASTEST (et du repli sans flou). */
const val GlassFlatFallbackAlpha = 0.86f

/** Rayon du flou par défaut (6 dp) ; le curseur « Givre » le remplace ensuite. */
val GlassBalancedBlurRadius = 6.dp

/**
 * Opacité minimale du film en BALANCED : avec un flou léger, le contenu derrière reste
 * lisible, le film doit donc être plus dense pour garder le texte contrasté.
 */
const val GlassBalancedMinFilmAlpha = 0.72f

/** Opacité du bruit de QUALITY (3 %). */
const val GlassNoiseOpacity = 0.03f
