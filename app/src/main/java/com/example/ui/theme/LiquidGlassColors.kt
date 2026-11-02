package com.example.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Style « Verre » : neutres bleutés et accent bleu ciel, pensés pour des surfaces translucides.
 * Les couleurs sont un choix de conception (la charte ne les fixe pas) : texte secondaire ≥ 6,5:1
 * sur le fond, en clair comme en sombre.
 */
val LiquidGlassDarkScheme = darkColorScheme(
    primary = Color(0xFF7AA2FF),
    onPrimary = Color(0xFF0B1020),
    primaryContainer = Color(0xFF22345F),
    onPrimaryContainer = Color(0xFFD6E2FF),

    secondary = Color(0xFFA8B3CF),
    onSecondary = Color(0xFF0B1020),
    secondaryContainer = Color(0xFF1C2540),
    onSecondaryContainer = Color(0xFFF2F5FF),

    tertiary = Color(0xFF7AA2FF),
    onTertiary = Color(0xFF0B1020),
    tertiaryContainer = Color(0xFF22345F),
    onTertiaryContainer = Color(0xFFD6E2FF),

    background = Color(0xFF0B1020),
    onBackground = Color(0xFFF2F5FF),

    surface = Color(0xFF141B2D),
    onSurface = Color(0xFFF2F5FF),
    surfaceVariant = Color(0xFF1C2540),
    onSurfaceVariant = Color(0xFFA8B3CF),

    surfaceTint = Color.Transparent,
    inverseSurface = Color(0xFFF2F5FF),
    inverseOnSurface = Color(0xFF0B1020),
    inversePrimary = Color(0xFF2F6FDE),

    error = Color(0xFFFF8A8A),
    onError = Color(0xFF0B1020),
    errorContainer = Color(0xFF5C1F1F),
    onErrorContainer = Color(0xFFFFD9D9),

    outline = Color(0xFF2A3556),
    outlineVariant = Color(0xFF2A3556)
)

val LiquidGlassLightScheme = lightColorScheme(
    primary = Color(0xFF2F6FDE),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E2FF),
    onPrimaryContainer = Color(0xFF0B2A66),

    secondary = Color(0xFF475569),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDE6F5),
    onSecondaryContainer = Color(0xFF0F172A),

    tertiary = Color(0xFF2F6FDE),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD6E2FF),
    onTertiaryContainer = Color(0xFF0B2A66),

    background = Color(0xFFEAF0FA),
    onBackground = Color(0xFF0F172A),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFDDE6F5),
    onSurfaceVariant = Color(0xFF475569),

    surfaceTint = Color.Transparent,
    inverseSurface = Color(0xFF0F172A),
    inverseOnSurface = Color(0xFFEAF0FA),
    inversePrimary = Color(0xFF7AA2FF),

    error = Color(0xFFB42318),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDA),
    onErrorContainer = Color(0xFF55120C),

    outline = Color(0xFFCAD6EC),
    outlineVariant = Color(0xFFCAD6EC)
)
