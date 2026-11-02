package com.example.data.preferences

/**
 * Teinte du verre (mise à jour 008).
 *
 * La teinte est un angle de teinte (hue) de 0 à 360° ; `null` signifie « Aucune » : le verre
 * garde alors uniquement son film. Quand une teinte est choisie, un voile de cette couleur est posé
 * sur le film avec une opacité de film x [GlassTintAlphaFactor].
 */

/** Part de l'opacité du film reprise par le voile teinté : alpha du voile = film x 0,5. */
const val GlassTintAlphaFactor = 0.5f

/** Saturation et luminosité de la couleur du voile (HSV), choisies pour garder le texte contrasté. */
const val GlassTintSaturation = 0.75f
const val GlassTintValue = 0.9f

/** Écart maximal (en degrés) pour qu'une teinte libre soit considérée comme une pastille. */
const val GlassTintPresetToleranceDeg = 1.5f

/** Pastilles proposées après « Aucune », dans l'ordre d'affichage. */
enum class GlassTintPreset(val hue: Float) {
    VIOLET(270f),
    RED(0f),
    ORANGE(30f),
    YELLOW(55f),
    GREEN(130f),
    CYAN(185f),
    BLUE(220f),
    MAGENTA(310f)
}

/** Ramène une teinte dans l'intervalle [0, 360[ (360 devient 0, -10 devient 350). */
fun clampGlassTintHue(hue: Float): Float {
    val modulo = hue % 360f
    return if (modulo < 0f) modulo + 360f else modulo
}

/** Lit la teinte enregistrée ; `null` (Aucune) si rien n'est enregistré ou si la valeur est invalide. */
fun resolveGlassTintHue(stored: Float?): Float? =
    stored?.takeIf { it.isFinite() }?.let(::clampGlassTintHue)

/** Opacité du voile teinté : opacité du film x 0,5. */
fun glassTintAlpha(filmAlpha: Float): Float =
    (filmAlpha * GlassTintAlphaFactor).coerceIn(0f, 1f)

/** Pastille correspondant à une teinte (à 1,5° près, en tenant compte du retour à 0°), sinon `null`. */
fun glassTintPresetFor(hue: Float?): GlassTintPreset? {
    if (hue == null) return null
    val value = clampGlassTintHue(hue)
    return GlassTintPreset.values().firstOrNull { preset ->
        val diff = kotlin.math.abs(value - preset.hue)
        minOf(diff, 360f - diff) <= GlassTintPresetToleranceDeg
    }
}
