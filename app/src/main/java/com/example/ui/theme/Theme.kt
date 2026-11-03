package com.example.ui.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Density
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.data.preferences.ListDensityPreference
import com.example.data.preferences.AccentChoice
import com.example.data.preferences.AppStyle
import com.example.data.preferences.AppThemeMode
import com.example.data.preferences.AppearanceMode
import com.example.ui.components.glass.LocalReduceAnimations
import com.example.ui.components.glass.rememberSystemAnimationsEnabled

val LocalMusicProListRowHeight = staticCompositionLocalOf { 64.dp }

/**
 * Palette de couleurs sombre Material 3 calquée fidèlement sur la charte MusicPro.
 */
val MusicProDarkColorScheme = darkColorScheme(
    primary = MusicProVioletPrimary,
    onPrimary = Color.White,
    primaryContainer = MusicProVioletSubtle,
    onPrimaryContainer = MusicProVioletPastel,

    secondary = MusicProCyanVibrant,
    onSecondary = Color.Black,
    secondaryContainer = Color(0x2600D4FF),
    onSecondaryContainer = MusicProCyanLight,

    tertiary = MusicProVioletLight,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0x267C3AED),
    onTertiaryContainer = MusicProVioletPastel,

    background = MusicProBackground,
    onBackground = MusicProTextPrimary,

    surface = MusicProSurface,
    onSurface = MusicProTextPrimary,
    surfaceVariant = MusicProSurfaceVariant,
    onSurfaceVariant = MusicProTextSecondary,

    surfaceTint = MusicProVioletPrimary,
    inverseSurface = MusicProTextPrimary,
    inverseOnSurface = MusicProBackground,

    error = MusicProFavorite,
    // Mise à jour 083 — onError doit atteindre 4,5:1 sur l'erreur ; le blanc plafonnait à 3,2:1.
    onError = MusicProBackground,
    errorContainer = MusicProFavoriteContainer,
    onErrorContainer = Color.White,

    outline = MusicProBorder,
    outlineVariant = MusicProBorderFaint
)

/**
 * Palette de secours (MusicPro étant un lecteur audio 100% sombre néon, le Dark theme est prépondérant).
 */
val MusicProLightColorScheme = lightColorScheme(
    primary = MusicProVioletPrimary,
    onPrimary = Color.White,
    primaryContainer = MusicProVioletPastel,
    onPrimaryContainer = Color(0xFF2E0066),

    secondary = MusicProCyanVibrant,
    onSecondary = Color.Black,

    tertiary = MusicProVioletVibrant,
    onTertiary = Color.White,

    background = Color(0xFFF7F7FA),
    onBackground = Color(0xFF0F0F19),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F0F19),
    surfaceVariant = Color(0xFFECEEF2),
    onSurfaceVariant = Color(0xFF4A4A5A),

    outline = Color(0xFF8A8F9A)
)

/**
 * Thème Jetpack Compose Material 3 pour MusicPro.
 */
@Composable
fun MusicProTheme(
    darkTheme: Boolean = true, // MusicPro est conçu pour une ambiance sombre néon
    dynamicColor: Boolean = false, // Par défaut désactivé pour respecter la charte exacte
    content: @Composable () -> Unit
) {
    val colorScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        runCatching {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }.getOrElse {
            if (darkTheme) MusicProDarkColorScheme else MusicProLightColorScheme
        }
    } else {
        if (darkTheme) MusicProDarkColorScheme else MusicProLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MusicProTypography,
        content = content
    )
}

/**
 * Thème complet (mises à jour 013 à 018) : apparence × style, accent, plage « Sombre le soir ».
 *
 *  - Les changements de style, d'apparence ou d'accent se fondent en 250 ms (accent « Selon la
 *    pochette » : 400 ms) ; avec « Réduire les animations », ils sont instantanés.
 *  - [eveningDark] vient de `SettingsViewModel.eveningDarkActive` : le thème bascule sans
 *    redémarrer l'activité.
 *  - L'accent ne s'applique qu'aux styles Minimal et Verre, et pas aux couleurs dynamiques.
 */
@Composable
fun MusicProTheme(
    appearance: AppearanceMode,
    style: AppStyle,
    accent: AccentChoice = AccentChoice.TERRACOTTA,
    eveningDark: Boolean = false,
    artworkAccent: Color? = null,
    dynamicColor: Boolean = false,
    fontScale: Float = 1f,
    listDensity: ListDensityPreference = ListDensityPreference.COMFORTABLE,
    reduceAnimations: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = resolveIsDark(appearance, eveningDark, systemDark)
    val context = LocalContext.current
    val systemAnimationsEnabled = rememberSystemAnimationsEnabled()
    val useDynamic = dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val baseScheme = if (useDynamic) {
        runCatching {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }.getOrElse { musicProBaseScheme(style, isDark) }
    } else {
        musicProBaseScheme(style, isDark)
    }

    val fadeMillis = when {
        !systemAnimationsEnabled -> 0
        reduceAnimations -> 120
        else -> MusicProTokens.Duration.ThemeFade
    }
    val accentMillis = when {
        !systemAnimationsEnabled -> 0
        reduceAnimations -> 120
        else -> MusicProTokens.Duration.AccentFade
    }
    val animatedBase = animateColorScheme(baseScheme, fadeMillis)

    val accentOverride = if (useDynamic) {
        null
    } else {
        resolveAccentOverride(accent, style, isDark, artworkAccent, dynamicColor = false)
    }
    val animatedAccent by animateColorAsState(
        targetValue = accentOverride ?: baseScheme.primary,
        animationSpec = tween(durationMillis = accentMillis),
        label = "theme_accent"
    )
    val colorScheme = if (accentOverride != null) {
        animatedBase.withAccent(animatedAccent, isDark)
    } else {
        animatedBase
    }

    val density = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(density = density.density, fontScale = density.fontScale * fontScale)) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = musicProTypography(style)
        ) {
            CompositionLocalProvider(
                LocalAppStyle provides style,
                LocalIsDarkTheme provides isDark,
                LocalMusicProListRowHeight provides listDensity.rowHeightDp.dp,
                LocalReduceAnimations provides reduceAnimations
            ) {
                content()
            }
        }
    }
}

@Composable
fun MusicProTheme(
    themeMode: AppThemeMode,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemDark
    }
    MusicProTheme(
        darkTheme = isDark,
        dynamicColor = dynamicColor,
        content = content
    )
}

/**
 * Alias pour rétrocompatibilité avec le template d'origine.
 */
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MusicProTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
        content = content
    )
}
