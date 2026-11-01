package com.example.data.preferences

/**
 * Niveau de l'effet verre choisi par l'utilisateur.
 *  - FASTEST  : aplat translucide, aucun flou.
 *  - BALANCED : flou léger (6 dp) et un seul reflet. Niveau par défaut.
 *  - QUALITY  : flou réel, reflet dégradé, bordure lumineuse, léger bruit (3 %) et ombre douce.
 *               Proposé uniquement à partir d'Android 12 (API 31).
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
