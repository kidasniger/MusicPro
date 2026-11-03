package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Mise à jour 065 — égaliseur animé (3 barres) signalant le morceau en cours de
 * lecture dans les listes. Les trois barres ondulent avec des phases décalées.
 *
 * @param animate false quand « Réduire les animations » est actif : les barres
 * restent alors figées à mi-hauteur (l'appelant peut aussi afficher une icône).
 */
@Composable
fun EqualizerIndicator(
    color: Color,
    modifier: Modifier = Modifier,
    barWidth: Dp = 4.dp,
    maxHeight: Dp = 18.dp,
    animate: Boolean = true
) {
    val transition = rememberInfiniteTransition(label = "equalizer")
    val phase1 by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(560, easing = LinearEasing), RepeatMode.Reverse),
        label = "eq1"
    )
    val phase2 by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.30f,
        animationSpec = infiniteRepeatable(tween(430, easing = LinearEasing), RepeatMode.Reverse),
        label = "eq2"
    )
    val phase3 by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(680, easing = LinearEasing), RepeatMode.Reverse),
        label = "eq3"
    )
    val levels = if (animate) listOf(phase1, phase2, phase3) else listOf(0.7f, 1f, 0.6f)

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
