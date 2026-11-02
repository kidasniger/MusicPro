package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.example.data.preferences.AppStyle

/**
 * Système typographique MusicPro.
 *
 * Familles (mise à jour 016) : [MusicProSansFamily] pour l'interface (Inter ou Instrument Sans) et
 * [MusicProSerifFamily] pour les grands titres du style Minimal (Source Serif 4 ou Newsreader).
 * Tant que les fichiers de police ne sont pas ajoutés dans `res/font`, ces deux familles se
 * rabattent sur les polices sans empattement et à empattement du système.
 *
 * Échelle des styles Minimal et Verre : Titre 28/34 semi-gras, Section 20/26, Corps 16/24,
 * Légende 13/18. Le style Néon garde son échelle d'origine.
 */

// Familles de polices : un seul endroit à modifier quand les fichiers .ttf sont embarqués.
val MusicProSansFamily: FontFamily = FontFamily.SansSerif
val MusicProSerifFamily: FontFamily = FontFamily.Serif

// Alias conservés pour le code existant.
val InterFontFamily = MusicProSansFamily
val SoraFontFamily = MusicProSansFamily
val LyricMonoFontFamily = FontFamily.Monospace

/** Typographie d'origine, utilisée par le style Néon. */
val MusicProTypography = Typography(
    // 32px • Titre Splash "MusicPro"
    displayLarge = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.5).sp,
        color = Color.Unspecified
    ),

    // 28px • Titres d'onboarding ("100% Hors ligne", "Paroles synchronisées")
    displayMedium = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.25).sp,
        color = Color.Unspecified
    ),

    // 22px • Titre en lecture NowPlaying & titres majeurs de pages ("Playlists", "Paramètres")
    headlineLarge = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.25).sp,
        color = Color.Unspecified
    ),

    // 20px • Ligne active mise en avant des paroles synchronisées (Karaoké actif)
    headlineMedium = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
        color = Color.Unspecified
    ),

    // 18px • En-têtes de modales ("Nouvelle playlist") & Dialogues
    headlineSmall = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        color = Color.Unspecified
    ),

    // 17px • Logo & Top Bar Header "MusicPro"
    titleLarge = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.2).sp,
        color = Color.Unspecified
    ),

    // 16px • Titres de rubriques ("Récemment écoutés"), Boutons CTA, Paroles inactives
    titleMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp,
        color = Color.Unspecified
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
        color = Color.Unspecified
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
        color = Color.Unspecified
    )
)

private fun softStyle(
    family: FontFamily,
    weight: FontWeight,
    size: Int,
    lineHeight: Int
) = TextStyle(
    fontFamily = family,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    color = Color.Unspecified
)

/**
 * Échelle douce des styles Minimal et Verre. Les couleurs ne sont pas fixées : le texte suit la
 * couleur de contenu du thème, donc il reste lisible en clair comme en sombre.
 */
private fun softTypography(titleFamily: FontFamily): Typography = Typography(
    displayLarge = softStyle(titleFamily, FontWeight.SemiBold, 28, 34),
    displayMedium = softStyle(titleFamily, FontWeight.SemiBold, 28, 34),
    displaySmall = softStyle(titleFamily, FontWeight.SemiBold, 28, 34),
    headlineLarge = softStyle(titleFamily, FontWeight.SemiBold, 28, 34),
    headlineMedium = softStyle(titleFamily, FontWeight.SemiBold, 20, 26),
    headlineSmall = softStyle(MusicProSansFamily, FontWeight.SemiBold, 20, 26),
    titleLarge = softStyle(MusicProSansFamily, FontWeight.SemiBold, 20, 26),
    titleMedium = softStyle(MusicProSansFamily, FontWeight.SemiBold, 16, 24),
    titleSmall = softStyle(MusicProSansFamily, FontWeight.SemiBold, 13, 18),
    bodyLarge = softStyle(MusicProSansFamily, FontWeight.Normal, 16, 24),
    bodyMedium = softStyle(MusicProSansFamily, FontWeight.Normal, 16, 24),
    bodySmall = softStyle(MusicProSansFamily, FontWeight.Normal, 13, 18),
    labelLarge = softStyle(MusicProSansFamily, FontWeight.Medium, 13, 18),
    labelMedium = softStyle(MusicProSansFamily, FontWeight.Medium, 13, 18),
    labelSmall = softStyle(MusicProSansFamily, FontWeight.Medium, 13, 18)
)

/** Minimal : grands titres en serif. */
val MinimalTypography = softTypography(MusicProSerifFamily)

/** Verre : tout en sans-serif. */
val LiquidGlassTypography = softTypography(MusicProSansFamily)

/** Typographie du style choisi. */
fun musicProTypography(style: AppStyle): Typography = when (style) {
    AppStyle.NEON -> MusicProTypography
    AppStyle.MINIMAL -> MinimalTypography
    AppStyle.GLASS -> LiquidGlassTypography
}

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
