package com.example.ui.components.glass

import android.os.Build
import androidx.compose.runtime.staticCompositionLocalOf

/** Vrai si l'appareil sait flouter en temps réel (RenderEffect, Android 12 / API 31 et plus). */
val glassBlurSupported: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

/**
 * Choix de l'utilisateur dans les réglages.
 * `false` correspond à l'option « Plus rapide » : aucun flou, aucun calque enregistré,
 * seulement un aplat translucide.
 */
val LocalGlassBlurAllowed = staticCompositionLocalOf { true }

/** Opacité de l'aplat translucide utilisé à la place du flou (API < 31 ou option « Plus rapide »). */
const val GlassFlatFallbackAlpha = 0.86f
