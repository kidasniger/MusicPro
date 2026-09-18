package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Charte graphique MusicPro extraite de MusicPro.html et du logo musicpro_logo_cutout.png.
 * Thème sombre néon haute fidélité (Dark Neon Synth / Electric Purple-Cyan).
 */

// ==========================================
// 1. COULEURS DE FOND & SURFACES (SURFACES & BACKGROUNDS)
// ==========================================
/** Fond principal de l'application (Container Background) - #07070F */
val MusicProBackground = Color(0xFF07070F)

/** Fond ultra sombre du canvas système - #050508 */
val MusicProBackgroundDeep = Color(0xFF050508)

/** Surface standard des cartes et panneaux (Glass Card) - #12121F */
val MusicProSurface = Color(0xFF12121F)

/** Surface semi-transparente pour effet Glassmorphism avec flou - 80% opacité */
val MusicProSurfaceGlass = Color(0xCC12121F)

/** Surface à 90% pour barres d'outils et bottom navigation */
val MusicProSurfaceNav = Color(0xE612121F)

/** Surface secondaire de carte / bordure de conteneur - #1A1A2E */
val MusicProSurfaceVariant = Color(0xFF1A1A2E)

/** Fond de conteneurs d'icônes et boutons de contrôle - #1E1E2F */
val MusicProSurfaceElevated = Color(0xFF1E1E2F)

/** Fond de carte MusicPro */
val MusicProCardBackground = Color(0xFF12121F)
val MusicProGreenEmerald = Color(0xFF10B981)

/** Surface sombre des modals et aperçu de code LRC - #0F0F19 */
val MusicProSurfaceCode = Color(0xFF0F0F19)

/** Surface système pour notification de lecture (Android MediaStyle) - #1C1C1E */
val MusicProNotificationBg = Color(0xFF1C1C1E)

/** Fond de l'îlot dynamique et noir absolu - #000000 */
val MusicProBlack = Color(0xFF000000)

// ==========================================
// 2. ACCENTS VIOLET NÉON (PRIMARY VIOLET ACCENTS)
// ==========================================
/** Violet néon principal (Signature Purple - BlueViolet) - #8A2BE2 */
val MusicProVioletPrimary = Color(0xFF8A2BE2)

/** Violet vibrant intermédiaire du dégradé - #7C3AED */
val MusicProVioletVibrant = Color(0xFF7C3AED)

/** Violet clair / lilas pour textes d'accentuation et badges - #A78BFA */
val MusicProVioletLight = Color(0xFFA78BFA)

/** Violet pastel lumineux pour titres en dégradé - #C4B5FD */
val MusicProVioletPastel = Color(0xFFC4B5FD)

/** Halo d'ombre néon violet (Glow 45% alpha) */
val MusicProVioletGlow = Color(0x738A2BE2)

/** Fond subtil violet pour sélection active (15% alpha) */
val MusicProVioletSubtle = Color(0x268A2BE2)

// ==========================================
// 3. ACCENTS CYAN NÉON (SECONDARY CYAN ACCENTS)
// ==========================================
/** Cyan électrique ultra lumineux - #00D4FF */
val MusicProCyanNeon = Color(0xFF00D4FF)

/** Cyan turquoise vif (Boutons d'action et progression) - #22D3EE */
val MusicProCyanVibrant = Color(0xFF22D3EE)

/** Cyan pastel clair pour icônes et textes - #67E8F9 */
val MusicProCyanLight = Color(0xFF67E8F9)

/** Halo d'ombre cyan néon (Glow 40% alpha) */
val MusicProCyanGlow = Color(0x6622D3EE)

// ==========================================
// 4. COULEURS DU DÉGRADÉ DU LOGO (LOGO SPECIFIC TONES)
// Extrait direct de musicpro_logo_cutout.png (du cyan au pourpre)
// ==========================================
/** Cyan éclatant de l'écouteur supérieur - #00E5FF */
val MusicProLogoCyan = Color(0xFF00E5FF)

/** Bleu roi néon de transition de l'arceau - #007BFF */
val MusicProLogoBlue = Color(0xFF007BFF)

/** Violet royal des barres centrales d'égaliseur - #8A2BE2 */
val MusicProLogoPurple = Color(0xFF8A2BE2)

/** Magenta électrique des pointes inférieures de l'onde - #9333EA */
val MusicProLogoMagenta = Color(0xFF9333EA)

// ==========================================
// 5. COULEURS SÉMANTIQUES (STATUS & FUNCTIONAL)
// ==========================================
/** Vert émeraude (100% hors-ligne actif, succès) - #10B981 */
val MusicProSuccess = Color(0xFF10B981)
val MusicProSuccessGlow = Color(0xCC10B981)
val MusicProSuccessContainer = Color(0x2610B981)

/** Ambre / Orange (Avertissement, cache ponctuel) - #F59E0B */
val MusicProWarning = Color(0xFFF59E0B)
val MusicProWarningContainer = Color(0x26F59E0B)

/** Rose néon (Favoris, cœur actif) - #FF4D6D */
val MusicProFavorite = Color(0xFFFF4D6D)
val MusicProFavoriteContainer = Color(0x33FF4D6D)

/** Rouge écarlate néon (Erreurs, quota dépassé, échec API) - #EF4444 */
val MusicProError = Color(0xFFEF4444)
val MusicProErrorContainer = Color(0x26EF4444)

/** Rose vif (Accents de playlists) - #FF6B9D */
val MusicProPinkAccent = Color(0xFFFF6B9D)

// ==========================================
// 6. TYPOGRAPHIE & TEXTES (TEXT & ON-SURFACE)
// ==========================================
/** Texte principal blanc 100% - #FFFFFF */
val MusicProTextPrimary = Color(0xFFFFFFFF)

/** Texte secondaire blanc 80% (Artistes, détails) */
val MusicProTextSecondary = Color(0xCCFFFFFF)

/** Texte tertiaire blanc 60% (Descriptions) */
val MusicProTextTertiary = Color(0x99FFFFFF)

/** Texte atténué blanc 40% (Compteurs, timestamps, placeholders) */
val MusicProTextMuted = Color(0x66FFFFFF)

/** Texte très discret blanc 20% (Index, mentions légales) */
val MusicProTextDisabled = Color(0x33FFFFFF)

/** Bordure principale subtile blanc 10% */
val MusicProBorder = Color(0x1AFFFFFF)

/** Bordure ultra-fine blanc 6% */
val MusicProBorderFaint = Color(0x0FFFFFFF)

// ==========================================
// 7. DÉGRADÉS COMPOSABLES (BRUSHES)
// ==========================================
/** Dégradé signature MusicPro (Violet néon vers Cyan néon) */
val MusicProPrimaryGradient = Brush.horizontalGradient(
    colors = listOf(MusicProVioletPrimary, MusicProCyanVibrant)
)

/** Dégradé complet du logo (Cyan -> Bleu -> Violet -> Magenta) */
val MusicProLogoGradient = Brush.linearGradient(
    colors = listOf(
        MusicProLogoCyan,
        MusicProLogoBlue,
        MusicProLogoPurple,
        MusicProLogoMagenta
    )
)

/** Dégradé textuel pour les grands titres d'impact */
val MusicProTextGradient = Brush.horizontalGradient(
    colors = listOf(MusicProVioletPastel, MusicProVioletPrimary, MusicProCyanVibrant)
)

/** Dégradé pour le fond des cartes (Dark Indigo Surface) */
val MusicProCardGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF1A1A2E), Color(0xFF12121F))
)

/** Dégradé de sélection active pour les paroles synchronisées */
val MusicProActiveLyricGradient = Brush.horizontalGradient(
    colors = listOf(Color(0x408A2BE2), Color(0x2622D3EE))
)
