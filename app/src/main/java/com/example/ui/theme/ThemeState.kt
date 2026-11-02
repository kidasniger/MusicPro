package com.example.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.data.preferences.AppStyle
import com.example.data.preferences.AppearanceMode

/** Style actif, fourni par [MusicProTheme] (Néon par défaut, comme avant). */
val LocalAppStyle = staticCompositionLocalOf { AppStyle.NEON }

/** Vrai si le thème réellement affiché est sombre (apparence résolue), fourni par [MusicProTheme]. */
val LocalIsDarkTheme = staticCompositionLocalOf { true }

/**
 * Apparence résolue. « Sombre le soir » est sombre dans la plage horaire ([eveningDark]) et clair
 * le reste du temps ; « Auto » suit le système.
 */
fun resolveIsDark(appearance: AppearanceMode, eveningDark: Boolean, systemDark: Boolean): Boolean =
    when (appearance) {
        AppearanceMode.LIGHT -> false
        AppearanceMode.DARK -> true
        AppearanceMode.AUTO -> systemDark
        AppearanceMode.DARK_EVENING -> eveningDark
    }

/** Schéma de couleurs de base d'un style dans un mode donné (sans accent personnalisé). */
fun musicProBaseScheme(style: AppStyle, isDark: Boolean): ColorScheme = when (style) {
    AppStyle.MINIMAL -> if (isDark) MinimalDarkScheme else MinimalLightScheme
    AppStyle.NEON -> if (isDark) MusicProDarkColorScheme else MusicProLightColorScheme
    AppStyle.GLASS -> if (isDark) LiquidGlassDarkScheme else LiquidGlassLightScheme
}

@Composable
private fun animatedColor(target: Color, durationMillis: Int): Color =
    animateColorAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = durationMillis),
        label = "scheme_color"
    ).value

/**
 * Anime chaque couleur du schéma vers [target] : un changement de style ou d'apparence se fond en
 * [durationMillis] ms au lieu de clignoter, sans recréer l'arbre de l'application (la navigation
 * et les états sont conservés). Durée 0 = changement instantané (« Réduire les animations »).
 */
@Composable
fun animateColorScheme(target: ColorScheme, durationMillis: Int): ColorScheme = target.copy(
    primary = animatedColor(target.primary, durationMillis),
    onPrimary = animatedColor(target.onPrimary, durationMillis),
    primaryContainer = animatedColor(target.primaryContainer, durationMillis),
    onPrimaryContainer = animatedColor(target.onPrimaryContainer, durationMillis),
    inversePrimary = animatedColor(target.inversePrimary, durationMillis),
    secondary = animatedColor(target.secondary, durationMillis),
    onSecondary = animatedColor(target.onSecondary, durationMillis),
    secondaryContainer = animatedColor(target.secondaryContainer, durationMillis),
    onSecondaryContainer = animatedColor(target.onSecondaryContainer, durationMillis),
    tertiary = animatedColor(target.tertiary, durationMillis),
    onTertiary = animatedColor(target.onTertiary, durationMillis),
    tertiaryContainer = animatedColor(target.tertiaryContainer, durationMillis),
    onTertiaryContainer = animatedColor(target.onTertiaryContainer, durationMillis),
    background = animatedColor(target.background, durationMillis),
    onBackground = animatedColor(target.onBackground, durationMillis),
    surface = animatedColor(target.surface, durationMillis),
    onSurface = animatedColor(target.onSurface, durationMillis),
    surfaceVariant = animatedColor(target.surfaceVariant, durationMillis),
    onSurfaceVariant = animatedColor(target.onSurfaceVariant, durationMillis),
    surfaceTint = animatedColor(target.surfaceTint, durationMillis),
    inverseSurface = animatedColor(target.inverseSurface, durationMillis),
    inverseOnSurface = animatedColor(target.inverseOnSurface, durationMillis),
    error = animatedColor(target.error, durationMillis),
    onError = animatedColor(target.onError, durationMillis),
    errorContainer = animatedColor(target.errorContainer, durationMillis),
    onErrorContainer = animatedColor(target.onErrorContainer, durationMillis),
    outline = animatedColor(target.outline, durationMillis),
    outlineVariant = animatedColor(target.outlineVariant, durationMillis),
    scrim = animatedColor(target.scrim, durationMillis)
)
