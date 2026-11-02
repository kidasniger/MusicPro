package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import com.example.data.preferences.AccentChoice
import com.example.data.preferences.AppStyle

/**
 * Accents (mise à jour 017). Chaque accent a une variante claire (texte blanc lisible dessus,
 * ≥ 4,5:1 sauf terracotta ≈ 3,9:1) et une variante sombre plus lumineuse.
 * ARTWORK n'a pas de couleur fixe : on retombe sur la terracotta tant qu'aucune pochette n'est lue.
 */
fun accentSeedColor(choice: AccentChoice, isDark: Boolean): Color = when (choice) {
    AccentChoice.TERRACOTTA, AccentChoice.ARTWORK ->
        if (isDark) MinimalDarkAccent else MinimalLightAccent
    AccentChoice.BLUE -> if (isDark) Color(0xFF7AA2FF) else Color(0xFF2F6FDE)
    AccentChoice.GREEN -> if (isDark) Color(0xFF6FCF97) else Color(0xFF2E7D5B)
    AccentChoice.VIOLET -> if (isDark) Color(0xFFB79CFF) else Color(0xFF7C4DDB)
}

/**
 * Couleur d'accent à superposer au schéma de base, ou `null` pour le laisser tel quel.
 * Le style Néon garde sa charte violet/cyan et les couleurs dynamiques (Material You) gardent
 * les leurs : l'accent ne s'applique qu'aux styles Minimal et Verre.
 */
fun resolveAccentOverride(
    choice: AccentChoice,
    style: AppStyle,
    isDark: Boolean,
    artworkAccent: Color?,
    dynamicColor: Boolean
): Color? {
    if (dynamicColor || style == AppStyle.NEON) return null
    return if (choice == AccentChoice.ARTWORK && artworkAccent != null) {
        artworkAccent
    } else {
        accentSeedColor(choice, isDark)
    }
}

fun contrastRatio(a: Color, b: Color): Float {
    val la = a.luminance()
    val lb = b.luminance()
    return (maxOf(la, lb) + 0.05f) / (minOf(la, lb) + 0.05f)
}

/** Blanc tant qu'il contraste ≥ 3,5:1 avec [background], sinon le plus lisible du blanc et du brun nuit. */
fun readableContentOn(background: Color): Color {
    val dark = Color(0xFF1A1915)
    val whiteContrast = contrastRatio(Color.White, background)
    if (whiteContrast >= 3.5f) return Color.White
    return if (contrastRatio(dark, background) > whiteContrast) dark else Color.White
}

/** Remplace la famille « primary » du schéma par [accent] et ses dérivées (texte dessus, conteneur). */
fun ColorScheme.withAccent(accent: Color, isDark: Boolean): ColorScheme {
    val onAccent = readableContentOn(accent)
    val container = lerp(surface, accent, if (isDark) 0.28f else 0.18f)
    return copy(
        primary = accent,
        onPrimary = onAccent,
        primaryContainer = container,
        onPrimaryContainer = onSurface,
        tertiary = accent,
        onTertiary = onAccent,
        tertiaryContainer = container,
        onTertiaryContainer = onSurface,
        inversePrimary = accent
    )
}
