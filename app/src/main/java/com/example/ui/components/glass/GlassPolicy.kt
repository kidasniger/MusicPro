package com.example.ui.components.glass

import android.os.Build
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp
import com.example.data.preferences.DefaultGlassColorStrength
import com.example.data.preferences.GlassMaterial

/** Vrai si l'appareil sait flouter en temps réel (RenderEffect, Android 12 / API 31 et plus). */
val glassBlurSupported: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

/** Niveau d'effet verre choisi dans les réglages ; fourni une fois pour toute l'application. */
val LocalGlassMaterial = staticCompositionLocalOf { GlassMaterial.BALANCED }

/**
 * Force de couleur du verre (0 à 100 %), réglée par le curseur « Couleur du verre ».
 * Elle pilote la saturation de ce qui transparaît derrière le verre (voir `glassSaturation`).
 */
val LocalGlassColorStrength = staticCompositionLocalOf { DefaultGlassColorStrength }

/** Opacité de l'aplat translucide de FASTEST (et du repli sans flou). */
const val GlassFlatFallbackAlpha = 0.86f

/** Rayon du flou du niveau BALANCED. */
val GlassBalancedBlurRadius = 6.dp

/**
 * Opacité minimale du film en BALANCED : avec seulement 6 dp de flou, le contenu derrière reste
 * lisible, le film doit donc être plus dense pour garder le texte contrasté.
 */
const val GlassBalancedMinFilmAlpha = 0.72f

/** Opacité du bruit de QUALITY (3 %). */
const val GlassNoiseOpacity = 0.03f
