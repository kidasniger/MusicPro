package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Système typographique MusicPro extrait de MusicPro.html.
 * - Police d'affichage / Display : Sora / Sans-Serif Bold
 * - Police de lecture / Interface : Inter / Sans-Serif Regular & Medium
 * - Police de code / Horodatage : Monospace pour les paroles LRC synchronisées
 */

// Familles de polices recommandées
val InterFontFamily = FontFamily.SansSerif
val SoraFontFamily = FontFamily.SansSerif
val LyricMonoFontFamily = FontFamily.Monospace

val MusicProTypography = Typography(
    // 32px • Titre Splash "MusicPro"
    displayLarge = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.5).sp,
        color = MusicProTextPrimary
    ),

    // 28px • Titres d'onboarding ("100% Hors ligne", "Paroles synchronisées")
    displayMedium = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.25).sp,
        color = MusicProTextPrimary
    ),

    // 22px • Titre en lecture NowPlaying & titres majeurs de pages ("Playlists", "Paramètres")
    headlineLarge = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.25).sp,
        color = MusicProTextPrimary
    ),

    // 20px • Ligne active mise en avant des paroles synchronisées (Karaoké actif)
    headlineMedium = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
        color = MusicProTextPrimary
    ),

    // 18px • En-têtes de modales ("Nouvelle playlist") & Dialogues
    headlineSmall = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        color = MusicProTextPrimary
    ),

    // 17px • Logo & Top Bar Header "MusicPro"
    titleLarge = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.2).sp,
        color = MusicProTextPrimary
    ),

    // 16px • Titres de rubriques ("Récemment écoutés"), Boutons CTA, Paroles inactives
    titleMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp,
        color = MusicProTextPrimary
    ),

    // 15px • Texte narratif d'introduction & widgets d'accueil
    titleSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        color = MusicProTextSecondary
    ),

    // 14px • Titres de morceaux dans les listes & champs de texte (Inputs)
    bodyLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
        color = MusicProTextPrimary
    ),

    // 13px • Noms d'artistes, sous-titres, menus secondaires, items de paramètres
    bodyMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = MusicProTextSecondary
    ),

    // 12px • Onglets Bibliothèque (Morceaux/Albums/Artistes), puces filtres ("Tous")
    bodySmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = MusicProTextTertiary
    ),

    // 11px • Durées audio ("3:20"), compteurs de pistes ("12 morceaux"), métadonnées compactes
    labelLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        color = MusicProTextMuted
    ),

    // 10px • Badges de statut en majuscules ("100% HORS LIGNE", "VERSION 1.0.0", "ÉGALISEUR")
    labelMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.sp,
        color = MusicProTextMuted
    ),

    // 9px • Micro-badges de format ("LRC", "SYNCED", tags minuscules)
    labelSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 9.sp,
        lineHeight = 12.sp,
        letterSpacing = 0.5.sp,
        color = MusicProTextPrimary
    )
)

/**
 * Style typographique pour la prévisualisation brute des fichiers .lrc (Karaoké brut).
 */
val LyricMonoTextStyle = TextStyle(
    fontFamily = LyricMonoFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 11.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.sp,
    color = MusicProTextTertiary
)
