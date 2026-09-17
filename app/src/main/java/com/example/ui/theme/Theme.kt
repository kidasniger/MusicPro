package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

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
    onError = Color.White,
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

    background = Color(0xFFF8F9FA),
    onBackground = Color(0xFF0F0F19),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F0F19),
    surfaceVariant = Color(0xFFECEEF2),
    onSurfaceVariant = Color(0xFF4A4A5A),

    outline = Color(0xFFCCCCCC)
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
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> MusicProDarkColorScheme
        else -> MusicProLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MusicProTypography,
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
