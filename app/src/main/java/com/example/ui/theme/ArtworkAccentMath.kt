package com.example.ui.theme

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Couleur dominante d'une pochette (mise à jour 017), sans bibliothèque externe : les pixels sont
 * répartis en 12 familles de teintes pondérées par la saturation et la luminosité ; la famille la
 * plus lourde gagne. Pochettes grisées ou presque noires : `null` (on garde l'accent par défaut).
 *
 * La couleur est ensuite ajustée pour servir d'accent : plus claire en thème sombre, plus foncée
 * en thème clair, afin de rester lisible sur le fond.
 *
 * @return couleur ARGB opaque, ou `null`.
 */
fun dominantAccentArgb(pixels: IntArray, isDark: Boolean): Int? {
    val bins = 12
    val weights = DoubleArray(bins)
    val sumR = DoubleArray(bins)
    val sumG = DoubleArray(bins)
    val sumB = DoubleArray(bins)

    for (pixel in pixels) {
        val alpha = (pixel ushr 24) and 0xFF
        if (alpha < 128) continue
        val r = (pixel shr 16) and 0xFF
        val g = (pixel shr 8) and 0xFF
        val b = pixel and 0xFF
        val hsv = rgbToHsv(r, g, b)
        val saturation = hsv[1]
        val value = hsv[2]
        if (saturation < 0.25 || value < 0.20) continue
        val weight = saturation * (0.4 + 0.6 * value)
        val bin = (hsv[0] / 30.0).toInt().coerceIn(0, bins - 1)
        weights[bin] += weight
        sumR[bin] += r * weight
        sumG[bin] += g * weight
        sumB[bin] += b * weight
    }

    var best = -1
    var bestWeight = 0.0
    for (i in 0 until bins) {
        if (weights[i] > bestWeight) {
            best = i
            bestWeight = weights[i]
        }
    }
    if (best < 0 || bestWeight < pixels.size * 0.01) return null

    val hsv = rgbToHsv(
        (sumR[best] / bestWeight).roundToInt().coerceIn(0, 255),
        (sumG[best] / bestWeight).roundToInt().coerceIn(0, 255),
        (sumB[best] / bestWeight).roundToInt().coerceIn(0, 255)
    )
    val saturation: Double
    val value: Double
    if (isDark) {
        saturation = hsv[1].coerceIn(0.45, 0.80)
        value = max(hsv[2], 0.80)
    } else {
        saturation = max(hsv[1], 0.50)
        value = min(hsv[2], 0.62)
    }
    return hsvToArgb(hsv[0], saturation, value)
}

private fun rgbToHsv(r: Int, g: Int, b: Int): DoubleArray {
    val rf = r / 255.0
    val gf = g / 255.0
    val bf = b / 255.0
    val maxC = max(rf, max(gf, bf))
    val minC = min(rf, min(gf, bf))
    val delta = maxC - minC
    val hue = when {
        delta == 0.0 -> 0.0
        maxC == rf -> 60.0 * (((gf - bf) / delta) % 6.0)
        maxC == gf -> 60.0 * (((bf - rf) / delta) + 2.0)
        else -> 60.0 * (((rf - gf) / delta) + 4.0)
    }
    val normalizedHue = if (hue < 0.0) hue + 360.0 else hue
    val saturation = if (maxC == 0.0) 0.0 else delta / maxC
    return doubleArrayOf(normalizedHue, saturation, maxC)
}

private fun hsvToArgb(hue: Double, saturation: Double, value: Double): Int {
    val chroma = value * saturation
    val hp = (hue / 60.0) % 6.0
    val x = chroma * (1.0 - abs(hp % 2.0 - 1.0))
    val m = value - chroma
    val rgb = when (hp.toInt()) {
        0 -> doubleArrayOf(chroma, x, 0.0)
        1 -> doubleArrayOf(x, chroma, 0.0)
        2 -> doubleArrayOf(0.0, chroma, x)
        3 -> doubleArrayOf(0.0, x, chroma)
        4 -> doubleArrayOf(x, 0.0, chroma)
        else -> doubleArrayOf(chroma, 0.0, x)
    }
    val r = ((rgb[0] + m) * 255.0).roundToInt().coerceIn(0, 255)
    val g = ((rgb[1] + m) * 255.0).roundToInt().coerceIn(0, 255)
    val b = ((rgb[2] + m) * 255.0).roundToInt().coerceIn(0, 255)
    return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
}
