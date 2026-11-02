package com.example.data.preferences

/**
 * Niveau de l'effet verre choisi par l'utilisateur.
 *  - FASTEST  : aplat translucide, aucun flou.
 *  - BALANCED : flou léger et un seul reflet. Niveau par défaut.
 *  - QUALITY  : flou réel, reflet dégradé, bordure lumineuse, léger bruit (3 %) et ombre douce.
 *               Proposé uniquement à partir d'Android 12 (API 31).
 *
 * Le rayon du flou des niveaux BALANCED et QUALITY est réglé par le curseur « Givre »
 * (voir [DefaultGlassFrostDp]).
 */
enum class GlassMaterial {
    FASTEST,
    BALANCED,
    QUALITY
}

/** Niveau réellement sélectionnable : QUALITY n'existe pas avant Android 12. */
fun GlassMaterial.effective(blurSupported: Boolean): GlassMaterial =
    if (!blurSupported && this == GlassMaterial.QUALITY) GlassMaterial.BALANCED else this

/** Vrai si ce niveau dessine un flou sur cet appareil. */
fun GlassMaterial.rendersBlur(blurSupported: Boolean): Boolean =
    blurSupported && this != GlassMaterial.FASTEST

/**
 * Lit le niveau enregistré, avec migration de l'ancienne clé `glass_quality` (mise à jour 004) :
 * FAST devient FASTEST, BEAUTIFUL devient QUALITY. Sans rien d'enregistré : BALANCED.
 */
fun resolveGlassMaterial(stored: String?, legacy: String?): GlassMaterial {
    val current = GlassMaterial.values().firstOrNull { it.name == stored }
    if (current != null) return current
    return when (legacy) {
        "FAST" -> GlassMaterial.FASTEST
        "BEAUTIFUL" -> GlassMaterial.QUALITY
        else -> GlassMaterial.BALANCED
    }
}

/** Force de couleur du verre par défaut, en pourcentage (curseur « Couleur du verre »). */
const val DefaultGlassColorStrength = 70

/** Ramène une force de couleur dans l'intervalle 0 à 100 %. */
fun clampGlassColorStrength(percent: Int): Int = percent.coerceIn(0, 100)

/** Lit la force de couleur enregistrée ; 70 % si rien n'est enregistré, valeur bornée à 0-100 %. */
fun resolveGlassColorStrength(stored: Int?): Int =
    clampGlassColorStrength(stored ?: DefaultGlassColorStrength)

/**
 * Saturation appliquée à ce qui transparaît derrière le verre :
 * 0,6 + 0,8 x force (force de 0 à 1), soit 0,6 à 0 % (neutre, désaturé) et 1,4 à 100 % (couleurs amplifiées).
 */
fun glassSaturation(percent: Int): Float =
    0.6f + 0.8f * (clampGlassColorStrength(percent) / 100f)

/** Givre par défaut, en dp : rayon du flou du verre. */
const val DefaultGlassFrostDp = 6

/** Bornes du curseur « Givre », en dp. */
const val MinGlassFrostDp = 0
const val MaxGlassFrostDp = 24

/** Sous ce rayon (en dp), le flou est désactivé pour économiser le GPU. */
const val GlassFrostBlurThresholdDp = 2

/** Ramène un givre dans l'intervalle 0 à 24 dp. */
fun clampGlassFrostDp(dp: Int): Int = dp.coerceIn(MinGlassFrostDp, MaxGlassFrostDp)

/** Lit le givre enregistré ; 6 dp si rien n'est enregistré, valeur bornée à 0-24 dp. */
fun resolveGlassFrostDp(stored: Int?): Int =
    clampGlassFrostDp(stored ?: DefaultGlassFrostDp)

/**
 * Vrai si un flou est réellement dessiné : niveau avec flou, appareil compatible (Android 12+)
 * et givre d'au moins [GlassFrostBlurThresholdDp] dp. Sinon : aplat translucide, et le fond
 * n'est ni enregistré ni mesuré.
 */
fun glassBlurActive(material: GlassMaterial, blurSupported: Boolean, frostDp: Int): Boolean =
    material.rendersBlur(blurSupported) && clampGlassFrostDp(frostDp) >= GlassFrostBlurThresholdDp
