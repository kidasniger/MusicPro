package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.components.glass.LocalReduceAnimations

/** Math pure de l'égaliseur (mise à jour 076), isolée pour rester testable. */
object EqualizerBars {
    /** Décalage de départ entre deux barres, en millisecondes. */
    const val STAGGER_MS = 120

    /** Niveau minimal (30 %) et maximal (100 %). */
    const val MIN_LEVEL = 0.30f
    const val MAX_LEVEL = 1.00f

    /** Nombre de barres. */
    const val BAR_COUNT = 3

    /**
     * Niveau d'une barre pour une phase `t` ∈ [0, 1] : sinusoïde compressée dans
     * [MIN_LEVEL, MAX_LEVEL]. Les phases des barres sont décalées de [STAGGER_MS].
     */
    fun levelFor(bar: Int, t: Float): Float {
        val phase = ((t + bar * STAGGER_MS / 1000f) % 1f + 1f) % 1f
        val wave = 0.5f - 0.5f * kotlin.math.cos(phase * 2f * kotlin.math.PI.toFloat())
        return MIN_LEVEL + (MAX_LEVEL - MIN_LEVEL) * wave
    }

    /** Niveaux figés affichés à la pause (l'animation s'arrête, les barres restent visibles). */
    fun frozenLevels(): List<Float> = listOf(0.45f, 0.85f, 0.60f)
}

/**
 * Mise à jour 076 — égaliseur animé (3 barres) signalant le morceau en cours.
 *
 * Les hauteurs oscillent entre 30 % et 100 %, décalées de 120 ms l'une de l'autre
 * (`StartOffset`). L'animation s'arrête à la pause : les barres se figent. Elle est
 * également désactivée lorsque « Réduire les animations » est actif ([animate] false).
 *
 * @param isPlaying faux à la pause : les barres se figent au lieu d'onduler.
 */
@Composable
fun EqualizerIndicator(
    color: Color,
    modifier: Modifier = Modifier,
    barWidth: Dp = 4.dp,
    maxHeight: Dp = 18.dp,
    isPlaying: Boolean = true,
    animate: Boolean = true
) {
    // À la pause ou sans animations : niveaux figés, aucune transition infinie composée.
    val animating = isPlaying && animate && !LocalReduceAnimations.current
    var levels by remember { mutableStateOf(EqualizerBars.frozenLevels()) }

    if (animating) {
        val transition = rememberInfiniteTransition(label = "equalizer_076")
        val wave by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(720, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "eq_wave"
        )
        levels = List(EqualizerBars.BAR_COUNT) { bar -> EqualizerBars.levelFor(bar, wave) }
    }

    Row(
        modifier = modifier.height(maxHeight),
        verticalAlignment = Alignment.Bottom
    ) {
        levels.forEachIndexed { index, level ->
            if (index > 0) Spacer(Modifier.width(barWidth / 2))
            Canvas(Modifier.size(barWidth, maxHeight)) {
                val barHeight = size.height * level
                drawRoundRect(
                    color = color,
                    topLeft = Offset(0f, size.height - barHeight),
                    size = Size(size.width, barHeight),
                    cornerRadius = CornerRadius(size.width / 2f, size.width / 2f)
                )
            }
        }
    }
}
