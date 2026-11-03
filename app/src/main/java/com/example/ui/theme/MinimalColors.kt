package com.example.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Style « Minimal » inspiré de Claude (mises à jour 011 et 012) : papier chaud, accent terracotta,
 * aucun dégradé, contours fins plutôt qu'ombres. C'est le style de référence des nouveaux écrans.
 *
 * Ces deux schémas ne sont pas encore branchés sur [MusicProTheme] : le choix du style arrive avec
 * la mise à jour 013 (réglages « Apparence » et « Style »).
 */

// ==========================================
// Clair (011)
// ==========================================
/** Fond crème chaud. */
val MinimalLightBackground = Color(0xFFF5F0E8)

/** Surface des cartes. */
val MinimalLightSurface = Color(0xFFFFFFFF)

/** Surface secondaire (champs, puces, zones discrètes). */
val MinimalLightSurfaceSecondary = Color(0xFFEDE7DB)

/** Texte principal. */
val MinimalLightText = Color(0xFF1F1E1B)

/** Texte secondaire. */
val MinimalLightTextSecondary = Color(0xFF6B665C)

/** Accent terracotta. Contraste vérifié (mise à jour 083) : 5,2:1 avec le blanc, 4,6:1 sur le fond crème. */
val MinimalLightAccent = Color(0xFFB14E2E)

/** Contour fin des cartes et séparateurs. */
val MinimalLightOutline = Color(0xFFE2DACB)

// ==========================================
// Sombre (012)
// ==========================================
/** Fond brun très foncé, jamais noir pur (évite l'effet qui bave sur OLED). */
val MinimalDarkBackground = Color(0xFF1A1915)

/** Surface des cartes. */
val MinimalDarkSurface = Color(0xFF232220)

/** Surface élevée (feuilles, menus, élément pressé). */
val MinimalDarkSurfaceElevated = Color(0xFF2C2A26)

/** Texte principal. */
val MinimalDarkText = Color(0xFFECE8DF)

/** Texte secondaire. */
val MinimalDarkTextSecondary = Color(0xFFA39E92)

/** Accent terracotta adouci pour fond sombre. */
val MinimalDarkAccent = Color(0xFFD97757)

/** Contour fin des cartes et séparateurs. */
val MinimalDarkOutline = Color(0xFF3A3732)

/**
 * Schéma clair Minimal. Les teintes non fournies par la charte (conteneur d'accent, erreur) sont
 * des déclinaisons discrètes de la terracotta. `surfaceTint` est transparent : pas de voile
 * d'élévation tonale, la hiérarchie passe par les contours.
 */
val MinimalLightScheme = lightColorScheme(
    primary = MinimalLightAccent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF3DDD3),
    onPrimaryContainer = Color(0xFF5A2514),

    secondary = MinimalLightTextSecondary,
    onSecondary = Color.White,
    secondaryContainer = MinimalLightSurfaceSecondary,
    onSecondaryContainer = MinimalLightText,

    tertiary = MinimalLightAccent,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF3DDD3),
    onTertiaryContainer = Color(0xFF5A2514),

    background = MinimalLightBackground,
    onBackground = MinimalLightText,

    surface = MinimalLightSurface,
    onSurface = MinimalLightText,
    surfaceVariant = MinimalLightSurfaceSecondary,
    onSurfaceVariant = MinimalLightTextSecondary,

    surfaceTint = Color.Transparent,
    inverseSurface = MinimalLightText,
    inverseOnSurface = MinimalLightBackground,
    inversePrimary = MinimalDarkAccent,

    error = Color(0xFFB42318),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDA),
    onErrorContainer = Color(0xFF55120C),

    outline = MinimalLightOutline,
    outlineVariant = MinimalLightOutline
)

/**
 * Schéma sombre Minimal. `onPrimary` est le fond sombre (contraste net sur la terracotta claire).
 */
val MinimalDarkScheme = darkColorScheme(
    primary = MinimalDarkAccent,
    onPrimary = MinimalDarkBackground,
    primaryContainer = Color(0xFF4A2D22),
    onPrimaryContainer = Color(0xFFF2C9B8),

    secondary = MinimalDarkTextSecondary,
    onSecondary = MinimalDarkBackground,
    secondaryContainer = MinimalDarkSurfaceElevated,
    onSecondaryContainer = MinimalDarkText,

    tertiary = MinimalDarkAccent,
    onTertiary = MinimalDarkBackground,
    tertiaryContainer = Color(0xFF4A2D22),
    onTertiaryContainer = Color(0xFFF2C9B8),

    background = MinimalDarkBackground,
    onBackground = MinimalDarkText,

    surface = MinimalDarkSurface,
    onSurface = MinimalDarkText,
    surfaceVariant = MinimalDarkSurfaceElevated,
    onSurfaceVariant = MinimalDarkTextSecondary,

    surfaceTint = Color.Transparent,
    inverseSurface = MinimalDarkText,
    inverseOnSurface = MinimalDarkBackground,
    inversePrimary = MinimalLightAccent,

    error = Color(0xFFF2877B),
    onError = MinimalDarkBackground,
    errorContainer = Color(0xFF5C1F18),
    onErrorContainer = Color(0xFFFAD4CF),

    outline = MinimalDarkOutline,
    outlineVariant = MinimalDarkOutline
)
